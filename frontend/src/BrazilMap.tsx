import { BRAZIL_STATES, BRAZIL_VIEWBOX } from './brazilStates'
import type { RegionMetrics, RegionStatus } from './useRegionStatus'

const fillByStatus: Record<RegionStatus, string> = {
  OK: 'fill-emerald-600',
  WARNING: 'fill-amber-500',
  CRITICAL: 'fill-red-600 animate-pulse',
}

// Estados pequenos ganham rotulo menor para nao sobrepor os vizinhos.
const SMALL_STATES = new Set(['RN', 'PB', 'PE', 'AL', 'SE', 'ES', 'RJ'])

type Props = {
  regions: Map<string, RegionMetrics>
  selected: string | null
  onSelect: (uf: string) => void
}

export default function BrazilMap({ regions, selected, onSelect }: Props) {
  return (
    <svg viewBox={BRAZIL_VIEWBOX} className="mx-auto h-auto max-h-[75vh] w-full" role="img" aria-label="Mapa do Brasil por status">
      {BRAZIL_STATES.map((state) => {
        const metrics = regions.get(state.uf)
        const fill = metrics ? fillByStatus[metrics.status] : 'fill-slate-700'
        const isSelected = selected === state.uf
        return (
          <path
            key={state.uf}
            d={state.d}
            className={`${fill} cursor-pointer stroke-slate-950 transition-colors hover:brightness-125 ${isSelected ? 'stroke-white' : ''}`}
            strokeWidth={isSelected ? 4 : 1.5}
            onClick={() => onSelect(state.uf)}
          >
            <title>
              {state.name}
              {metrics ? ` — ${(metrics.rebufferRate * 100).toFixed(1)}% de travamento` : ''}
            </title>
          </path>
        )
      })}
      {BRAZIL_STATES.filter((s) => s.uf !== 'DF').map((state) => (
        <text
          key={`label-${state.uf}`}
          x={state.cx}
          y={state.cy}
          textAnchor="middle"
          dominantBaseline="middle"
          className="pointer-events-none fill-white font-semibold"
          fontSize={SMALL_STATES.has(state.uf) ? 11 : 15}
        >
          {state.uf}
        </text>
      ))}
    </svg>
  )
}
