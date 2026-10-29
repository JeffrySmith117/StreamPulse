import { useRegionStatus, type RegionStatus } from './useRegionStatus'

const statusStyle: Record<RegionStatus, string> = {
  OK: 'bg-emerald-600',
  WARNING: 'bg-amber-500',
  CRITICAL: 'bg-red-600 animate-pulse',
}

export default function App() {
  const { regions, connected } = useRegionStatus()
  const critical = regions.filter((r) => r.status === 'CRITICAL').length
  const warning = regions.filter((r) => r.status === 'WARNING').length

  return (
    <main className="min-h-screen bg-slate-950 text-slate-100 p-8">
      <header className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">StreamPulse</h1>
        <span className="flex items-center gap-2 text-sm text-slate-400">
          <span className={`h-2.5 w-2.5 rounded-full ${connected ? 'bg-emerald-500' : 'bg-red-500'}`} />
          {connected ? 'Ao vivo' : 'Reconectando...'}
        </span>
      </header>

      <p className="mt-2 text-slate-400">
        {regions.length} estados monitorados · {critical} críticos · {warning} em atenção
      </p>

      <ul className="mt-8 grid grid-cols-3 gap-3 sm:grid-cols-5 lg:grid-cols-9">
        {regions.map((r) => (
          <li
            key={r.region}
            className={`rounded-lg p-3 text-center ${statusStyle[r.status]}`}
            title={`Tempo médio travado: ${(r.avgBufferMs / 1000).toFixed(1)}s`}
          >
            <div className="text-lg font-bold">{r.region}</div>
            <div className="text-sm opacity-90">{(r.rebufferRate * 100).toFixed(1)}%</div>
          </li>
        ))}
      </ul>
    </main>
  )
}