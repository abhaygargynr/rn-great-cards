import React from 'react';
import {Text, View} from 'react-native';

import {styles} from '../styles';

type SummaryStatProps = {
  label: string;
  value: string;
};

export const SummaryStat = ({label, value}: SummaryStatProps) => {
  return (
    <View style={styles.summaryStat}>
      <Text style={styles.summaryLabel}>{label}</Text>
      <Text style={styles.summaryValue}>{value}</Text>
    </View>
  );
};
