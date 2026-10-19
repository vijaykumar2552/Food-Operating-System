// Picks a food emoji ("sticker") for an item based on its name or category.
const MAP: [RegExp, string][] = [
  [/pizza/i, "🍕"],
  [/burger/i, "🍔"],
  [/noodle|hakka|ramen|chow/i, "🍜"],
  [/biryani|rice|pulao|curry/i, "🍛"],
  [/salad|healthy|bowl/i, "🥗"],
  [/cake|pastry|brownie/i, "🍰"],
  [/ice ?cream|sundae|kulfi/i, "🍨"],
  [/dessert|sweet/i, "🧁"],
  [/sandwich|wrap|roll/i, "🥪"],
  [/fries|side/i, "🍟"],
  [/shake|drink|juice|smoothie|cola|soda/i, "🥤"],
  [/coffee|tea/i, "☕"],
  [/pasta|spaghetti|penne|italian/i, "🍝"],
  [/bread|garlic/i, "🥖"],
  [/momo|dumpling|chinese/i, "🥟"],
  [/kebab|tikka|starter|grill|hyderabadi/i, "🍢"],
  [/taco|mexican/i, "🌮"],
  [/american/i, "🍔"],
];

export function foodEmoji(...hints: (string | undefined)[]): string {
  const text = hints.filter(Boolean).join(" ");
  for (const [re, e] of MAP) if (re.test(text)) return e;
  return "🍽️";
}

const TONES = ["bg-sticker-1", "bg-sticker-2", "bg-sticker-3", "bg-sticker-4", "bg-sticker-5", "bg-sticker-6"];
export function stickerTone(seed: string | number): string {
  const s = String(seed);
  let h = 0;
  for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) >>> 0;
  return TONES[h % TONES.length];
}

export const formatPrice = (n: number) => `₹${n.toLocaleString("en-IN", { maximumFractionDigits: 2 })}`;

export const CATEGORIES = [
  { label: "Pizza", emoji: "🍕" },
  { label: "Burgers", emoji: "🍔" },
  { label: "Noodles", emoji: "🍜" },
  { label: "Biryani", emoji: "🍛" },
  { label: "Salads", emoji: "🥗" },
  { label: "Desserts", emoji: "🍰" },
  { label: "Sandwiches", emoji: "🥪" },
  { label: "Fries", emoji: "🍟" },
  { label: "Drinks", emoji: "🥤" },
];
