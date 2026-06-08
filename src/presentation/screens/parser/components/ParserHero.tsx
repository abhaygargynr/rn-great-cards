import React from 'react';
import {Text} from 'react-native';

import {STRINGS} from '../../../../localization/strings';
import {styles} from '../styles';

export const ParserHero = () => {
  return (
    <>
      <Text style={styles.eyebrow}>{STRINGS.parser.eyebrow}</Text>
      <Text style={styles.title}>{STRINGS.parser.title}</Text>
      <Text style={styles.subtitle}>{STRINGS.parser.subtitle}</Text>
    </>
  );
};
