import { useCallback, useEffect, useState } from 'react'

/**
 * Controla o simulador do back-end: lista os estados degradados
 * e permite degradar ou restaurar um estado pela tela.
 */
export function useSimulator() {
  const [degraded, setDegraded] = useState<string[]>([])

  const refresh = useCallback(async () => {
    try {
      const res = await fetch('/api/simulator/regions/degraded')
      if (res.ok) setDegraded(await res.json())
    } catch {
      // back-end fora do ar: mantem a lista atual
    }
  }, [])

  useEffect(() => {
    fetch('/api/simulator/regions/degraded')
      .then((res) => (res.ok ? res.json() : null))
      .then((list: string[] | null) => {
        if (list) setDegraded(list)
      })
      .catch(() => {})
  }, [])

  const toggle = useCallback(
    async (uf: string) => {
      const method = degraded.includes(uf) ? 'DELETE' : 'POST'
      await fetch(`/api/simulator/regions/${uf}/degrade`, { method })
      await refresh()
    },
    [degraded, refresh],
  )

  return { degraded, toggle }
}
