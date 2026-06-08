import type {ParsedResult, SmsSample} from '../../../types';

export type RowItem = {
  sample: SmsSample;
  result: ParsedResult;
};

export type ParserSummary = {
  includedCount: number;
  excludedCount: number;
  inrDebitTotal: number;
  inrCreditRefundTotal: number;
  excludeCounts: Array<[string, number]>;
};
