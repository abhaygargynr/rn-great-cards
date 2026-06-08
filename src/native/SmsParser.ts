import {NativeModules} from 'react-native';

import type {ParsedResult} from '../types';

type SmsParserModuleShape = {
  parseSms(samples: string[]): Promise<ParsedResult[]>;
};

const {SmsParserModule} = NativeModules as {
  SmsParserModule?: SmsParserModuleShape;
};

export async function parseSms(samples: string[]): Promise<ParsedResult[]> {
  if (!SmsParserModule?.parseSms) {
    throw new Error('SmsParserModule is not available on this platform.');
  }

  return SmsParserModule.parseSms(samples);
}
