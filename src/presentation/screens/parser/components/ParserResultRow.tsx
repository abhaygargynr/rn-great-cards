import React from 'react';
import {Pressable, Text, View} from 'react-native';

import {STRINGS} from '../../../../localization/strings';
import {ParserDecision} from '../../../../types';
import {getBankInitials} from '../helper';
import type {RowItem} from '../types';
import {styles} from '../styles';

type ParserResultRowProps = {
  item: RowItem;
  onPress: (item: RowItem) => void;
};

export const ParserResultRow = ({item, onPress}: ParserResultRowProps) => {
  const {result, sample} = item;
  const transaction = result.transaction;
  const isIncluded = result.decision === ParserDecision.INCLUDE;

  return (
    <Pressable
      style={[styles.row, !isIncluded && styles.rowExcluded]}
      onPress={() => onPress(item)}>
      <View style={styles.rowLeading}>
        <View
          style={[
            styles.avatar,
            isIncluded ? styles.avatarInclude : styles.avatarExclude,
          ]}>
          <Text style={styles.avatarText}>
            {isIncluded
              ? getBankInitials(transaction?.bank)
              : STRINGS.parser.excludedAvatar}
          </Text>
        </View>
      </View>

      <View style={styles.rowBody}>
        {isIncluded && transaction ? (
          <>
            <View style={styles.rowTopLine}>
              <Text style={styles.rowTitle}>
                {transaction.merchant ?? STRINGS.parser.unknownMerchant}
              </Text>
              <Text style={styles.amountText}>
                {transaction.currency} {transaction.amount.toFixed(2)}
              </Text>
            </View>
            <Text style={styles.rowMeta}>
              {transaction.bank} • {transaction.type}
              {transaction.cardLastFour
                ? ` • ${STRINGS.parser.cardPrefix} ${transaction.cardLastFour}`
                : ''}
              {transaction.date ? ` • ${transaction.date}` : ''}
            </Text>
          </>
        ) : (
          <>
            <View style={styles.rowTopLine}>
              <Text style={styles.rowTitle}>
                {STRINGS.parser.samplePrefix} {sample.id}
              </Text>
              <View style={styles.excludeBadge}>
                <Text style={styles.excludeBadgeText}>
                  {result.excludeReason ?? STRINGS.parser.unknownKey}
                </Text>
              </View>
            </View>
            <Text style={styles.rowMeta} numberOfLines={2}>
              {STRINGS.parser.previewPrefix} {result.rawSms}
            </Text>
          </>
        )}
      </View>

      <View style={styles.rowTrailing}>
        <Text style={styles.confidenceLabel}>
          {(result.confidence * 100).toFixed(0)}%
        </Text>
      </View>
    </Pressable>
  );
};
