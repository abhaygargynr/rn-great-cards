import React from 'react';
import {Modal, Pressable, ScrollView, Text, View} from 'react-native';

import {STRINGS} from '../../../../localization/strings';
import {SectionTitle} from '../../../atoms/SectionTitle';
import type {RowItem} from '../types';
import {styles} from '../styles';
import {DetailRow} from './DetailRow';

type ParserDetailsModalProps = {
  selected: RowItem | null;
  onClose: () => void;
};

export const ParserDetailsModal = ({
  selected,
  onClose,
}: ParserDetailsModalProps) => {
  const transaction = selected?.result.transaction;

  return (
    <Modal
      animationType="slide"
      transparent
      visible={selected !== null}
      onRequestClose={onClose}>
      <View style={styles.modalBackdrop}>
        <View style={styles.modalCard}>
          <SectionTitle>
            {selected
              ? `${STRINGS.parser.samplePrefix} ${selected.sample.id}`
              : STRINGS.parser.details}
          </SectionTitle>
          {selected ? (
            <ScrollView>
              <DetailRow
                label={STRINGS.parser.decision}
                value={selected.result.decision}
              />
              <DetailRow
                label={STRINGS.parser.excludeReason}
                value={selected.result.excludeReason ?? STRINGS.parser.none}
              />
              <DetailRow
                label={STRINGS.parser.confidence}
                value={`${(selected.result.confidence * 100).toFixed(1)}%`}
              />
              <DetailRow label={STRINGS.parser.rawSms} value={selected.result.rawSms} />
              {transaction ? (
                <>
                  <DetailRow label={STRINGS.parser.bank} value={transaction.bank} />
                  <DetailRow
                    label={STRINGS.parser.amount}
                    value={`${transaction.currency} ${transaction.amount.toFixed(2)}`}
                  />
                  <DetailRow
                    label={STRINGS.parser.merchant}
                    value={transaction.merchant ?? STRINGS.parser.unknownMerchant}
                  />
                  <DetailRow label={STRINGS.parser.type} value={transaction.type} />
                  <DetailRow
                    label={STRINGS.parser.cardLastFour}
                    value={transaction.cardLastFour ?? STRINGS.parser.missing}
                  />
                  <DetailRow
                    label={STRINGS.parser.date}
                    value={transaction.date ?? STRINGS.parser.missing}
                  />
                </>
              ) : null}
            </ScrollView>
          ) : null}

          <Pressable style={styles.closeButton} onPress={onClose}>
            <Text style={styles.closeButtonText}>{STRINGS.parser.close}</Text>
          </Pressable>
        </View>
      </View>
    </Modal>
  );
};
