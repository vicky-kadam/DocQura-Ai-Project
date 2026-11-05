// Backend se aane wale citation / search match ko safe tareeke se padhne ke helpers.

export const citationFile = (c) =>
  c?.fileName || c?.metadata?.fileName || c?.metadata?.filename || 'Unknown file';

// Similarity 0..100 (number) ya null agar backend ne score nahi bheja.
export const citationPercent = (c) => {
  let s = c?.similarityScore;
  if (s == null && typeof c?.metadata?.distance === 'number') s = 1 - c.metadata.distance;
  if (typeof s !== 'number' || Number.isNaN(s)) return null;
  const pct = s <= 1 ? s * 100 : s; // accepts 0..1 or 0..100
  return Math.max(0, Math.min(100, Math.round(pct)));
};