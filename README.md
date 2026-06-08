# Bank SMS Parser Assignment

React Native Android app with a native Kotlin parser that classifies Indian bank SMS messages into structured credit-card transaction results.

This implementation is intentionally conservative:

- false positives are worse than false negatives
- exclusion rules run before extraction
- the parser only includes messages it can defend as completed credit-card transactions
- bank attribution is resolved from the SMS body, not from sender/app branding

Expected visible result target:

- included count: `7`
- excluded count: `18`
- included sample IDs: `2, 5, 7, 8, 9, 21, 22`

## Screen Recording

Add your recording link here before submission:

- `TODO: paste Loom / Drive link`

## How To Run

Install dependencies:

```sh
yarn install
```

Start Metro:

```sh
yarn start
```

Run Android:

```sh
yarn android
```

If Metro is not already running, use:

```sh
yarn start
yarn android
```

Run Kotlin parser tests:

```sh
yarn test:android
```

Run the React Native render test:

```sh
npx jest --runInBand --watchman=false
```

Run TypeScript checks:

```sh
yarn typecheck
```

## What Is In The Repo

- `android/app/src/main/java/com/rngreatcards/parser/`
  Kotlin parser, extractors, config, and RN bridge.
- `android/app/src/test/java/com/rngreatcards/parser/SmsParserTest.kt`
  Kotlin unit tests covering key cases and the visible sample counts.
- `data/samples.json`
  The 25 visible assignment samples used by the screen.
- `src/native/SmsParser.ts`
  TypeScript bridge wrapper for the native module.
- `App.tsx`
  Single-screen assignment UI with summary, rows, and detail modal.

## Assignment Mapping

The app screen covers the required UI surface:

- summary header with included count, excluded count, INR debit total, INR credit/refund total, and exclusion counts
- result list with separate included and excluded row treatments
- detail modal with raw SMS, decision, exclude reason, parsed transaction fields, and confidence

The parser tests also assert the visible assignment target directly:

- exactly `7` includes
- exactly `18` excludes
- included IDs are `2, 5, 7, 8, 9, 21, 22`

## Parsing Architecture

### Kotlin parser location

The parser lives under `android/app/src/main/java/com/rngreatcards/parser/`.

Main pieces:

- `SmsParser.kt`
  Orchestrates classification, extraction, and confidence scoring.
- `SmsNormalizer.kt`
  Builds a normalized lowercase / compact representation for matching while preserving raw SMS.
- `ParseConfig.kt`
  Holds bank aliases, currency aliases, date regexes, stop tokens, and exclusion confidence defaults.
- `SmsExtractors.kt`
  Contains the bank resolver, currency + amount extractor, merchant extractor, date extractor, and card-last-four extractor.
- `SmsParserModule.kt`
  Exposes the Kotlin parser to React Native through a native module.

### React Native bridge

The bridge method is:

- `SmsParserModule.parseSms(samples: ReadableArray, promise: Promise)`

On the JS side, `src/native/SmsParser.ts` calls that native method and returns:

- `Promise<ParsedResult[]>`

The React Native screen loads `data/samples.json`, extracts the `text` field from each sample, passes the list to Kotlin, and renders the returned schema directly.

### Why classification comes before extraction

I split the parser mentally into two phases:

1. Decide whether the SMS should be excluded immediately.
2. Only if it survives exclusion, attempt inclusion detection and structured extraction.

That matters because many excluded SMS bodies still contain merchants, amounts, and even the word `card`. For example:

- savings-account debits to merchants
- UPI messages
- declined attempts
- EMI conversions
- fees and finance charges

If extraction ran first and classification second, it would be much easier to create false positives.

### How exclusion logic is separated from extraction logic

`SmsParser.detectEarlyExclusion(...)` handles the high-priority exclusion pass:

- OTP
- OFFER
- SERVICE_MESSAGE
- LIMIT_UPDATE
- DECLINED
- AUTH_HOLD
- FUTURE_AUTO_DEBIT
- BILL_DUE
- CARD_PAYMENT
- EMI_CONVERSION
- FEE_OR_CHARGE
- DEBIT_CARD
- UPI_BANK_ACCOUNT
- BALANCE_ALERT
- SAVINGS_ACCOUNT
- INVESTMENT
- INSURANCE
- REWARD_OR_CASHBACK

Only after this pass does `detectIncludeCandidate(...)` consider:

- `REFUND`
- `DEBIT`

Then the extractor layer pulls:

- bank
- amount
- currency
- merchant
- card last four
- date

### How bank detection works

Bank detection is resolved from SMS body aliases stored in `ParseConfig.bankRules`.

Examples:

- `Edge Federal Bank Credit Card` resolves to `Federal Bank`
- `BOBCARD One Credit Card` resolves to `BOBCARD / Bank of Baroda`
- `HDFC Card xx5678` resolves to `HDFC Bank`

The resolver does not depend on sender IDs. That avoids common fintech/co-branded mistakes like attributing sample 8 to Jupiter instead of Federal Bank.

### Where config lives

The parser is config-driven through `ParseConfig.kt`.

That file currently contains:

- bank rules
- currency aliases
- date regexes
- merchant stop tokens
- exclusion confidence defaults

This keeps the parser extensible without turning the whole solution into one giant regex.

### How to add a new bank

Add one `BankRule` in `ParseConfig.bankRules`, for example:

```kotlin
BankRule("HSBC Bank", listOf("hsbc", "hsbc bank", "hsbc credit card"))
```

No parser logic needs to change if the new bank follows existing extraction conventions.

### How to add a new exclusion rule

Add a new rule branch inside `detectEarlyExclusion(...)`, then add its default confidence in `ParseConfig.exclusionConfidence`.

If the rule introduces new vocabulary that should also influence extraction, update only the relevant extractor instead of mixing extraction logic into the classifier.

### Why I structured it this way

The assignment weights hidden-sample resilience, conservative behavior, and explainability more than clever regex tricks. This structure makes it easier to:

- reason about rule order
- extend banks and signals
- test parser behavior in isolation
- keep JS thin and Kotlin-native logic central

## Confidence Scoring

Confidence is heuristic and combines:

- how sure the parser is about the decision
- how complete the extracted fields are

### Include confidence

Include confidence starts from `0.75` and adjusts for:

- explicit `credit card` phrasing
- generic card with credit-card evidence
- issuer bank resolved
- amount extracted
- currency extracted
- merchant extracted
- card last four extracted
- date extracted

It is reduced for:

- generic card ambiguity
- missing bank
- missing merchant
- missing date
- missing card last four
- multiple amount candidates
- mixed account/card signals
- ambiguous refund wording

Clear includes cap at `0.95`.

### Exclusion confidence

Exclusions use consistent defaults from `ParseConfig.exclusionConfidence`.

Examples:

- `OTP` -> `0.98`
- `DEBIT_CARD` -> `0.95`
- `UPI_BANK_ACCOUNT` -> `0.94`
- `DECLINED` -> `0.96`
- `MALFORMED_SMS` -> `0.10`
- `LOW_CONFIDENCE` -> `0.40`

### Malformed and low-confidence handling

Malformed or truncated SMS messages fail safely:

- `decision = EXCLUDE`
- `excludeReason = MALFORMED_SMS`
- `transaction = null`
- low confidence

If a message is not clearly malformed but still cannot be safely classified as a real credit-card transaction, it becomes:

- `EXCLUDE / LOW_CONFIDENCE`

## Samples I Struggled With

### Sample 1

It mentions `BLOCK CC`, which is noisy footer text that could trick a naive card parser. The important part is the source:

- `From HDFC Bank A/C *4521`

That is a savings-account transaction, so it must stay excluded.

### Sample 5

This is intentionally tricky because it says generic `Axis Bank Card` instead of `Credit Card`. The deciding evidence is:

- `Available Limit`

That is strong credit-card context, so it is included.

### Sample 8

This is the key co-branded case:

- the user-facing app is Jupiter
- the issuer in the SMS body is Federal Bank

The parser must not attribute the spend to Jupiter.

### Sample 22

This is another generic `Card` message, but it is still a real card spend because:

- it is phrased as a completed spend
- it names the issuer bank
- it includes foreign-currency markup / statement language

It also must preserve `USD` instead of collapsing to `INR`.

### Sample 25

This is intentionally broken input. The parser should not guess. It should fail closed as `MALFORMED_SMS`.

## What I Would Do Differently With A Full Week

- move from hand-authored conditional rules to a cleaner rule-engine abstraction with richer match metadata
- expand bank alias coverage and separate issuer, network, and product branding more explicitly
- improve merchant extraction with token scoring and merchant cleanup rules
- add a dedicated amount candidate model with positional weights instead of the current windowed heuristic
- broaden date parsing and locale handling
- add more hidden-style synthetic test samples to stress false-positive boundaries
- separate config into bundled JSON so non-code rule edits become easier
- add parser telemetry hooks for unknown patterns and `LOW_CONFIDENCE` clusters
- add screenshot-based UI regression checks for the result list and modal

## Production Android Design Note

In production, I would keep parsing fully on-device and make the SMS ingestion flow incremental. On first run, the app would explain why SMS access is needed, show a narrow value proposition, and only then request runtime permission. If the user denies permission, the app should still remain usable in a limited mode, clearly explain what feature is blocked, and offer a retry path from settings rather than hard-failing the whole app. Because Google Play treats SMS access as high-risk, I would design the product assuming policy scrutiny: request only the minimum permission set, document the user benefit plainly, and keep parsing local with no raw SMS upload by default.

Beyond the initial deterministic rules engine, I would also evaluate a small TensorFlow Lite layer for production use. The important constraint is that inference should still happen on-device. A practical model lifecycle would be: host a versioned TFLite file, let the app download it ahead of time, validate checksum/signature, cache it locally, and then execute inference fully offline on the device. That means there is no per-SMS network call for parsing, and once the model is downloaded the runtime path behaves like local code execution. I would not let the model fully replace the Kotlin rules engine. Instead, I would use the rules as the primary safety net and use the TFLite model as a second-pass classifier, a low-confidence resolver, or a helper for harder merchant / issuer extraction cases. If model download fails, storage is constrained, the model is unavailable, or model confidence is weak, the parser should continue to operate entirely on deterministic local rules. I would also version models carefully, support rollback, and gate rollout so parser regressions can be contained quickly without requiring a full app release.

For ingestion, I would separate bootstrap from steady-state operation. Bootstrap would read a bounded recent window of SMS messages to build the initial card history. After that, the app should parse only incremental arrivals. The lowest-latency path for the 30-second notification requirement is a direct `BroadcastReceiver` listening for new SMS delivery, doing lightweight classification immediately, and then handing off deeper work to a short-lived background component. That path can realistically meet the latency target because it runs at message arrival time. A pure periodic `WorkManager` poll cannot reliably meet 30 seconds because scheduling is opportunistic and deferred under system policy. A `ContentObserver` can help detect inbox changes, but by itself it is not the strongest real-time trigger across OEM variants.

Duplicate prevention should combine multiple keys: normalized body, sender, timestamp bucket, and if available message database ID. Parsed outputs should be written with an idempotency key so retried jobs or process restarts do not create duplicate spends or duplicate notifications. If the app process dies mid-parse, the receiver path should persist a small pending job record and a `WorkManager` retry should reconcile it later. That gives immediate best-effort behavior plus durable recovery.

OEM behavior on Indian Android devices needs explicit product handling. Xiaomi / MIUI / HyperOS, Realme, OPPO / ColorOS, Vivo / FuntouchOS, and OnePlus / OxygenOS all add aggressive battery optimization, autostart controls, background execution limits, standby bucketing, and sometimes delayed or suppressed broadcast behavior. Technically, that means the app can miss or delay real-time work if the user has not exempted it from OEM background controls. The UX should detect these restrictions, explain them in plain language, and guide the user through enablement only when needed. I would build a status surface that says whether real-time card alerts are healthy, degraded, or blocked, then show OEM-specific education steps such as enabling autostart, disabling battery optimization for this app, and allowing background activity. The goal is not only to ask for permissions, but to help users recover when the device vendor silently throttles the app.

With a 30-second notification target, the parser has to run locally on the device in the arrival path. A server-roundtrip design or periodic background polling would not be reliable enough. If the app is killed or delayed by OEM policy, the product should degrade gracefully: store the SMS for later reconciliation, surface that alerts may be delayed, and recover as soon as the app becomes schedulable again. In short, the production design would be a hybrid: immediate receiver-based parsing for best latency, persistent retry infrastructure for resilience, and explicit OEM education to handle the realities of Indian Android devices.

## AI Tool Usage

AI tools used:

- GPT-based coding assistance during parser design and implementation

What prompts worked well:

- asking for conservative parser architecture instead of direct regex output
- asking for hidden-sample-safe heuristics
- asking for co-branded issuer attribution rules and exclusion-first flow

What prompts failed or were weak:

- prompts that only asked for “parse these SMS with regex”
- prompts that overfit to visible samples without architecture
- prompts that ignored exclusion ordering and confidence scoring

What AI materially helped write:

- the initial parser structure
- the first draft of rule coverage
- the README framework

What I changed or rewrote manually:

- tightening the generic-card inclusion logic
- aligning the parser with the visible `7 include / 18 exclude` requirement
- handling the refund and foreign-currency cases more conservatively
- wiring the React Native native bridge and the assignment screen

What I verified manually:

- Kotlin unit tests
- visible sample include/exclude counts
- TypeScript typecheck
- Jest render test
- Android build/install flow

## Known Limitations

- merchant extraction is best-effort and intentionally heuristic
- bank alias coverage is limited to the current assignment scope
- confidence scoring is hand-tuned, not learned
- hidden samples outside similar wording families may still fall into `LOW_CONFIDENCE`
- screen recording link is still a manual submission step

## Submission Checklist Mapping

- React Native Android app: yes
- Kotlin native parser module: yes
- JS bridge call to `parseSms`: yes
- rendering all 25 parsed results: yes
- summary header: yes
- detail modal: yes
- config-driven bank/exclusion rules: yes
- Kotlin parser unit tests: yes
- README sections: yes
- production Android design note: yes
- screen recording link: placeholder only, add before submission
- no real SMS permission request: yes
- no external parsing API dependency: yes
