// Provides reusable FoodOS frontend helpers for meta behavior.
// Keeps shared presentation or data logic outside route components.

export function pageMeta(title: string, description: string) {
  const t = `${title} · FoodOS`;
  return {
    meta: [
      { title: t },
      { name: "description", content: description },
      { property: "og:title", content: t },
      { property: "og:description", content: description },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  };
}
