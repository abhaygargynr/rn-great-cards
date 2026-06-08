import {STRINGS} from '../../../localization/strings';
import {ParserDecision, TransactionType} from '../../../types';
import type {ParserSummary, RowItem} from './types';

export function buildParserSummary(items: RowItem[]): ParserSummary {
  let includedCount = 0;
  let excludedCount = 0;
  let inrDebitTotal = 0;
  let inrCreditRefundTotal = 0;
  const reasonMap = new Map<string, number>();

  items.forEach(({result}) => {
    if (result.decision === ParserDecision.INCLUDE && result.transaction) {
      includedCount += 1;
      if (result.transaction?.currency === 'INR') {
        if (result.transaction.type === TransactionType.DEBIT) {
          inrDebitTotal += result.transaction.amount;
        }
        if (
          result.transaction.type === TransactionType.CREDIT ||
          result.transaction.type === TransactionType.REFUND
        ) {
          inrCreditRefundTotal += result.transaction.amount;
        }
      }
      return;
    }

    excludedCount += 1;
    const key = result.excludeReason ?? STRINGS.parser.unknownKey;
    reasonMap.set(key, (reasonMap.get(key) ?? 0) + 1);
  });

  return {
    includedCount,
    excludedCount,
    inrDebitTotal,
    inrCreditRefundTotal,
    excludeCounts: [...reasonMap.entries()].sort((left, right) => right[1] - left[1]),
  };
}

export function getBankInitials(bank?: string | null) {
  if (!bank) {
    return STRINGS.parser.unknownInitials;
  }

  return bank
    .replace('/', ' ')
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map(part => part[0]?.toUpperCase() ?? '')
    .join('');
}
