import React from 'react';
import {View} from 'react-native';

import {STRINGS} from '../../../../localization/strings';
import {SectionTitle} from '../../../atoms/SectionTitle';
import type {RowItem} from '../types';
import {styles} from '../styles';
import {ParserResultRow} from './ParserResultRow';

type ParserResultsListProps = {
  items: RowItem[];
  onSelect: (item: RowItem) => void;
};

export const ParserResultsList = ({
  items,
  onSelect,
}: ParserResultsListProps) => {
  return (
    <View style={styles.listCard}>
      <SectionTitle>{STRINGS.parser.results}</SectionTitle>
      {items.map(item => (
        <ParserResultRow key={item.sample.id} item={item} onPress={onSelect} />
      ))}
    </View>
  );
};
