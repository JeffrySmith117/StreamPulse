import { useEffect, useState } from 'react'
import { Client } from '@stomp/stompjs'

export type RegionStatus = 'OK' | 'WARNING' | 'CRITICAL'

export type RegionMetrics = {
  region: string
  totalEvents: number
  bufferEvents: number
  errorEvents: number
  rebufferRate: number
  avgBufferMs: number
  errorRate: number
  status: RegionStatus
  updatedAt: string
}

/**
 * Busca o status inicial pela API REST e depois recebe as atualizacoes
 * em tempo real pelo WebSocket (topico /topic/regions), a cada segundo.
 */
export function useRegionStatus() {
  const [regions, setRegions] = useState<RegionMetrics[]>([])
  const [connected, setConnected] = useState(false)

  useEffect(() => {
    fetch('/api/regions/status')
      .then((res) => res.json())
      .then(setRegions)
      .catch(() => {})

    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
    const client = new Client({
      brokerURL: `${protocol}://${window.location.host}/ws`,
      reconnectDelay: 3000,
      onConnect: () => {
        setConnected(true)
        client.subscribe('/topic/regions', (message) => {
          setRegions(JSON.parse(message.body))
        })
      },
      onWebSocketClose: () => setConnected(false),
    })
    client.activate()

    return () => {
      void client.deactivate()
    }
  }, [])

  return { regions, connected }
}