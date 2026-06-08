import {StyleSheet} from 'react-native';

export const styles = StyleSheet.create({
  eyebrow: {
    color: '#8d5b2b',
    fontSize: 13,
    fontWeight: '700',
    letterSpacing: 1,
    textTransform: 'uppercase',
  },
  title: {
    color: '#1f2328',
    fontSize: 30,
    fontWeight: '800',
  },
  subtitle: {
    color: '#5b6470',
    fontSize: 15,
    lineHeight: 22,
  },
  summaryCard: {
    backgroundColor: '#fffaf2',
    borderRadius: 24,
    padding: 18,
    shadowColor: '#000',
    shadowOpacity: 0.08,
    shadowRadius: 12,
    shadowOffset: {width: 0, height: 8},
    elevation: 4,
    gap: 16,
  },
  summaryGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
  },
  summaryStat: {
    width: '47%',
    backgroundColor: '#fff',
    borderRadius: 18,
    padding: 14,
    gap: 6,
  },
  summaryLabel: {
    color: '#6c7785',
    fontSize: 12,
    fontWeight: '600',
    textTransform: 'uppercase',
  },
  summaryValue: {
    color: '#173046',
    fontSize: 20,
    fontWeight: '800',
  },
  reasonWrap: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  reasonTitle: {
    width: '100%',
    color: '#5d6774',
    fontSize: 12,
    fontWeight: '700',
    textTransform: 'uppercase',
  },
  reasonChip: {
    backgroundColor: '#20324a',
    borderRadius: 999,
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  reasonChipText: {
    color: '#f8fbff',
    fontSize: 12,
    fontWeight: '700',
  },
  listCard: {
    backgroundColor: '#fff',
    borderRadius: 24,
    padding: 18,
    gap: 14,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#e8e2d8',
    borderRadius: 18,
    padding: 14,
    gap: 12,
  },
  rowExcluded: {
    backgroundColor: '#f7f5f1',
  },
  rowLeading: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  avatar: {
    width: 44,
    height: 44,
    borderRadius: 22,
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarInclude: {
    backgroundColor: '#dbeaf8',
  },
  avatarExclude: {
    backgroundColor: '#f1ddd8',
  },
  avatarText: {
    color: '#20324a',
    fontSize: 14,
    fontWeight: '800',
  },
  rowBody: {
    flex: 1,
    gap: 6,
  },
  rowTopLine: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
  },
  rowTitle: {
    flex: 1,
    color: '#1f2328',
    fontSize: 16,
    fontWeight: '700',
  },
  rowMeta: {
    color: '#66707d',
    fontSize: 13,
    lineHeight: 18,
  },
  amountText: {
    color: '#2056d8',
    fontSize: 15,
    fontWeight: '800',
  },
  excludeBadge: {
    backgroundColor: '#f3d7ca',
    borderRadius: 999,
    paddingHorizontal: 10,
    paddingVertical: 6,
  },
  excludeBadgeText: {
    color: '#7c2f16',
    fontSize: 11,
    fontWeight: '800',
  },
  rowTrailing: {
    alignItems: 'flex-end',
  },
  confidenceLabel: {
    color: '#6d7682',
    fontSize: 12,
    fontWeight: '700',
  },
  loadingState: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  loadingText: {
    color: '#586170',
    fontSize: 14,
  },
  errorText: {
    color: '#a02727',
    fontSize: 14,
    lineHeight: 20,
  },
  modalBackdrop: {
    flex: 1,
    backgroundColor: 'rgba(21, 24, 28, 0.42)',
    justifyContent: 'flex-end',
    padding: 16,
  },
  modalCard: {
    maxHeight: '85%',
    backgroundColor: '#fffdf8',
    borderRadius: 28,
    padding: 20,
    gap: 16,
  },
  detailRow: {
    gap: 4,
    marginBottom: 12,
  },
  detailLabel: {
    color: '#7a8490',
    fontSize: 12,
    fontWeight: '700',
    textTransform: 'uppercase',
  },
  detailValue: {
    color: '#1f2328',
    fontSize: 15,
    lineHeight: 22,
  },
  closeButton: {
    backgroundColor: '#20324a',
    borderRadius: 16,
    paddingVertical: 14,
    alignItems: 'center',
  },
  closeButtonText: {
    color: '#fff',
    fontSize: 15,
    fontWeight: '800',
  },
});
