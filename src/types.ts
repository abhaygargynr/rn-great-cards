export enum ParserDecision {
  INCLUDE = 'INCLUDE',
  EXCLUDE = 'EXCLUDE',
}

export enum TransactionType {
  DEBIT = 'DEBIT',
  CREDIT = 'CREDIT',
  REFUND = 'REFUND',
}

export type ParsedTransaction = {
  amount: number;
  currency: string;
  bank: string;
  cardLastFour: string | null;
  merchant: string | null;
  type: TransactionType;
  date: string | null;
};

export type ParsedResult = {
  rawSms: string;
  decision: ParserDecision;
  excludeReason: string | null;
  transaction: ParsedTransaction | null;
  confidence: number;
};

export type SmsSample = {
  id: number;
  text: string;
};
