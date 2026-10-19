// Resolves stored image identifiers to browser object URLs for React views.
// An effect tracks the identifier and revokes temporary URLs when they change.

import { useState, useEffect } from 'react'
import { imageUrl } from '../lib/db'

export function useImageUrl(imageId: string | null): string | null {
  const [url, setUrl] = useState<string | null>(null)

  useEffect(() => {
    let revoke: string | null = null
    let active = true

    async function load() {
      if (!imageId) { setUrl(null); return }
      const u = await imageUrl(imageId)
      if (active) {
        setUrl(u)
        revoke = u
      }
    }
    load()

    return () => {
      active = false
      if (revoke) URL.revokeObjectURL(revoke)
    }
  }, [imageId])

  return url
}
