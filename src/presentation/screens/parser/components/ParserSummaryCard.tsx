import React from 'react';
import {ActivityIndicator, Text, View} from 'react-native';

import {STRINGS} from '../../../../localization/strings';
import {SectionTitle} from '../../../atoms/SectionTitle';
import {formatInr} from '../../../../utils/formatters';
import type {ParserSummary} from '../types';
import {styles} from '../styles';
import {SummaryStat} from './SummaryStat';

type ParserSummaryCardProps = {
  error: string | null;
  loading: boolean;
  summary: ParserSummary;
};

export const ParserSummaryCard = ({
  error,
  loading,
  summary,
}: ParserSummaryCardProps) => {
  return (
    <View style={styles.summaryCard}>
      <SectionTitle>{STRINGS.parser.summary}</SectionTitle>
      {loading ? (
        <View style={styles.loadingState}>
          <ActivityIndicator color="#2056d8" />
          <Text style={styles.loadingText}>{STRINGS.parser.parsingSamples}</Text>
        </View>
      ) : error ? (
        <Text style={styles.errorText}>{error}</Text>
      ) : (
        <>
          <View style={styles.summaryGrid}>
            <SummaryStat
              label={STRINGS.parser.included}
              value={String(summary.includedCount)}
            />
            <SummaryStat
              label={STRINGS.parser.excluded}
              value={String(summary.excludedCount)}
            />
            <SummaryStat
              label={STRINGS.parser.inrDebit}
              value={formatInr(summary.inrDebitTotal)}
            />
            <SummaryStat
              label={STRINGS.parser.inrCreditRefund}
              value={formatInr(summary.inrCreditRefundTotal)}
            />
          </View>
          <View style={styles.reasonWrap}>
            <Text style={styles.reasonTitle}>{STRINGS.parser.topExclusions}</Text>
            {summary.excludeCounts.map(([reason, count]) => (
              <View key={reason} style={styles.reasonChip}>
                <Text style={styles.reasonChipText}>
                  {reason}: {count}
                </Text>
              </View>
            ))}
          </View>
        </>
      )}
    </View>
  );
};
