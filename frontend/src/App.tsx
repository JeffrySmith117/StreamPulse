import { useMemo, useState } from 'react'
import BrazilMap from './BrazilMap'
import { BRAZIL_STATES } from './brazilStates'
import { useRegionStatus, type RegionMetrics, type RegionStatus } from './useRegionStatus'
import { useSimulator } from './useSimulator'

const badgeByStatus: Record<RegionStatus, string> = {
  OK: 'bg-emerald-600',
  WARNING: 'bg-amber-500',
  CRITICAL: 'bg-red-600',
}

const labelByStatus: Record<RegionStatus, string> = {
  OK: 'Normal',
  WARNING: 'Atenção',
  CRITICAL: 'Crítico',
}

const stateName = new Map(BRAZIL_STATES.map((s) => [s.uf, s.name]))

const percent = (value: number) => `${(value * 100).toFixed(1)}%`

export default function App() {
  const { regions, connected } = useRegionStatus()
  const { degraded, toggle } = useSimulator()
  const [selected, setSelected] = useState<string | null>('PI')

  const byUf = useMemo(() => new Map(regions.map((r) => [r.region, r])), [regions])
  const problems = useMemo(
    () =>
      regions
        .filter((r) => r.status !== 'OK')
        .sort((a, b) => b.rebufferRate - a.rebufferRate),
    [regions],
  )
  const critical = regions.filter((r) => r.status === 'CRITICAL').length
  const warning = regions.filter((r) => r.status === 'WARNING').length
  const current: RegionMetrics | undefined = selected ? byUf.get(selected) : undefined

  return (
    <main className="min-h-screen bg-slate-950 p-6 text-slate-100 lg:p-8">
      <header className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold">StreamPulse</h1>
          <p className="mt-1 text-slate-400">
            {regions.length} estados monitorados · {critical} críticos · {warning} em atenção
          </p>
        </div>
        <span className="flex items-center gap-2 text-sm text-slate-400">
          <span className={`h-2.5 w-2.5 rounded-full ${connected ? 'bg-emerald-500' : 'bg-red-500'}`} />
          {connected ? 'Ao vivo' : 'Reconectando...'}
        </span>
      </header>

      <div className="mt-6 grid gap-6 lg:grid-cols-[minmax(0,1fr)_360px]">
        <section className="rounded-xl border border-slate-800 bg-slate-900/50 p-4">
          <BrazilMap regions={byUf} selected={selected} onSelect={setSelected} />
          <div className="mt-3 flex gap-4 text-sm text-slate-400">
            {(['OK', 'WARNING', 'CRITICAL'] as RegionStatus[]).map((s) => (
              <span key={s} className="flex items-center gap-2">
                <span className={`h-3 w-3 rounded-sm ${badgeByStatus[s]}`} />
                {labelByStatus[s]}
              </span>
            ))}
          </div>
        </section>

        <aside className="flex flex-col gap-6">
          <section className="rounded-xl border border-slate-800 bg-slate-900/50 p-5">
            {selected && current ? (
              <>
                <div className="flex items-start justify-between">
                  <div>
                    <p className="text-sm text-slate-400">Estado selecionado</p>
                    <h2 className="text-xl font-semibold">
                      {stateName.get(selected)} ({selected})
                    </h2>
                  </div>
                  <span className={`rounded px-2 py-1 text-xs font-semibold ${badgeByStatus[current.status]}`}>
                    {labelByStatus[current.status]}
                  </span>
                </div>
                <dl className="mt-4 grid grid-cols-2 gap-4">
                  <div>
                    <dt className="text-xs text-slate-400">Taxa de travamento</dt>
                    <dd className="text-2xl font-bold">{percent(current.rebufferRate)}</dd>
                  </div>
                  <div>
                    <dt className="text-xs text-slate-400">Tempo médio travado</dt>
                    <dd className="text-2xl font-bold">{(current.avgBufferMs / 1000).toFixed(1)}s</dd>
                  </div>
                  <div>
                    <dt className="text-xs text-slate-400">Taxa de erro</dt>
                    <dd className="text-2xl font-bold">{percent(current.errorRate)}</dd>
                  </div>
                  <div>
                    <dt className="text-xs text-slate-400">Eventos (30s)</dt>
                    <dd className="text-2xl font-bold">{current.totalEvents}</dd>
                  </div>
                </dl>
                <button
                  type="button"
                  onClick={() => void toggle(selected)}
                  className={`mt-5 w-full rounded-lg px-4 py-2 font-semibold transition-colors ${
                    degraded.includes(selected)
                      ? 'bg-slate-700 hover:bg-slate-600'
                      : 'bg-red-600 hover:bg-red-500'
                  }`}
                >
                  {degraded.includes(selected) ? 'Restaurar estado' : 'Simular falha neste estado'}
                </button>
              </>
            ) : (
              <p className="text-slate-400">Clique em um estado no mapa para ver os detalhes.</p>
            )}
          </section>

          <section className="rounded-xl border border-slate-800 bg-slate-900/50 p-5">
            <h2 className="font-semibold">Estados com problema</h2>
            {problems.length === 0 ? (
              <p className="mt-3 text-sm text-slate-400">Nenhum problema detectado.</p>
            ) : (
              <ul className="mt-3 flex flex-col gap-2">
                {problems.map((r) => (
                  <li key={r.region}>
                    <button
                      type="button"
                      onClick={() => setSelected(r.region)}
                      className="flex w-full items-center justify-between rounded-lg bg-slate-800/60 px-3 py-2 text-left hover:bg-slate-800"
                    >
                      <span className="flex items-center gap-2">
                        <span className={`h-2.5 w-2.5 rounded-full ${badgeByStatus[r.status]}`} />
                        {stateName.get(r.region)} ({r.region})
                      </span>
                      <span className="font-semibold">{percent(r.rebufferRate)}</span>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </aside>
      </div>
    </main>
  )
}
