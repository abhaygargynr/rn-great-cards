import {startTransition, useEffect, useMemo, useState} from 'react';

import sampleData from '../../../../../data/samples.json';
import {STRINGS} from '../../../../localization/strings';
import {parseSms} from '../../../../native/SmsParser';
import {ParserDecision} from '../../../../types';
import {buildParserSummary} from '../helper';
import type {RowItem} from '../types';

export const useParserResults = () => {
  const [results, setResults] = useState<RowItem[]>([]);
  const [selected, setSelected] = useState<RowItem | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadResults = async () => {
      try {
        const smsTexts = sampleData?.map(sample => sample?.text ?? '') ?? [];
        const parsed = await parseSms(smsTexts);

        startTransition(() => {
          setResults(
            (sampleData ?? []).map((sample, index) => ({
              sample,
              result: parsed?.[index] ?? {
                rawSms: sample?.text ?? '',
                decision: ParserDecision.EXCLUDE,
                excludeReason: STRINGS.parser.unknownKey,
                transaction: null,
                confidence: 0,
              },
            })),
          );
          setError(null);
        });
      } catch (loadError) {
        setError(
          loadError instanceof Error
            ? loadError.message
            : STRINGS.parser.parseErrorFallback,
        );
      } finally {
        setLoading(false);
      }
    };

    void loadResults();
  }, []);

  const summary = useMemo(() => buildParserSummary(results), [results]);

  return {
    error,
    loading,
    results,
    selected,
    setSelected,
    summary,
  };
};
