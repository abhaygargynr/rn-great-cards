import React from 'react';
import {StyleProp, StyleSheet, View, ViewStyle} from 'react-native';
import {
  NativeSafeAreaViewProps,
  useSafeAreaInsets,
} from 'react-native-safe-area-context';

interface ScreenWrapperProps extends NativeSafeAreaViewProps {
  style?: StyleProp<ViewStyle>;
  children?: React.ReactNode;
}

export const ScreenWrapper = ({
  style,
  children = null,
  ...props
}: ScreenWrapperProps) => {
  const insets = useSafeAreaInsets();

  return (
    <View
      style={StyleSheet.flatten([
        styles.base,
        {
          paddingTop: insets.top,
          paddingLeft: insets.left,
          paddingRight: insets.right,
        },
        style,
      ])}
      {...props}>
      {children}
    </View>
  );
};

const styles = StyleSheet.create({
  base: {
    flex: 1,
    backgroundColor: '#f3efe6',
  },
});
