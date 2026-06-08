import React from 'react';
import {StyleSheet, Text, TextProps} from 'react-native';

type SectionTitleProps = TextProps & {
  children: React.ReactNode;
};

export const SectionTitle = ({children, style, ...rest}: SectionTitleProps) => {
  return (
    <Text {...rest} style={[styles.title, style]}>
      {children}
    </Text>
  );
};

const styles = StyleSheet.create({
  title: {
    color: '#1f2328',
    fontSize: 18,
    fontWeight: '800',
  },
});
