import { useEffect, useState } from 'react'

type RegionMetrics = {
  region: string
  status: 'OK' | 'WARNING' | 'CRITICAL'
  rebufferRate: number
}

export default function App() {
  const [regions, setRegions] = useState<RegionMetrics[]>([])

  useEffect(() => {
    fetch('/api/regions/status')
      .then((res) => res.json())
      .then(setRegions)
  }, [])

  const critical = regions.filter((r) => r.status === 'CRITICAL')

  return (
    <main className="min-h-screen bg-slate-950 text-slate-100 p-8">
      <h1 className="text-3xl font-bold">StreamPulse</h1>
      <p className="mt-2 text-slate-400">
        {regions.length} estados monitorados · {critical.length} em estado crítico
      </p>
      <ul className="mt-6 flex flex-wrap gap-2">
        {critical.map((r) => (
          <li key={r.region} className="rounded bg-red-600 px-3 py-1 font-semibold">
            {r.region} · {(r.rebufferRate * 100).toFixed(1)}%
          </li>
        ))}
      </ul>
    </main>
  )
}