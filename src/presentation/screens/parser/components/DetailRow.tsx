import React from 'react';
import {Text, View} from 'react-native';

import {styles} from '../styles';

type DetailRowProps = {
  label: string;
  value: string;
};

export const DetailRow = ({label, value}: DetailRowProps) => {
  return (
    <View style={styles.detailRow}>
      <Text style={styles.detailLabel}>{label}</Text>
      <Text style={styles.detailValue}>{value}</Text>
    </View>
  );
};
