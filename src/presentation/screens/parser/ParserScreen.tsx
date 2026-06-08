import React, {useCallback} from 'react';
import {ScrollView, StyleSheet} from 'react-native';

import {ScreenWrapper} from '../../atoms/screenWrapper';
import {useParserResults} from './hooks/useParserResults';
import {ParserDetailsModal} from './components/ParserDetailsModal';
import {ParserHero} from './components/ParserHero';
import {ParserResultsList} from './components/ParserResultsList';
import {ParserSummaryCard} from './components/ParserSummaryCard';
import type {RowItem} from './types';

export const ParserScreen = () => {
  const {error, loading, results, selected, setSelected, summary} =
    useParserResults();
  const handleSelect = useCallback((item: RowItem) => {
    setSelected(item);
  }, [setSelected]);
  const handleClose = useCallback(() => {
    setSelected(null);
  }, [setSelected]);

  return (
    <>
      <ScreenWrapper>
        <ScrollView contentContainerStyle={styles.content}>
          <ParserHero />
          <ParserSummaryCard
            error={error}
            loading={loading}
            summary={summary}
          />
          <ParserResultsList items={results} onSelect={handleSelect} />
        </ScrollView>
      </ScreenWrapper>
      <ParserDetailsModal selected={selected} onClose={handleClose} />
    </>
  );
};

const styles = StyleSheet.create({
  content: {
    padding: 20,
    paddingBottom: 40,
    gap: 16,
  },
});
