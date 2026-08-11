import { useEffect, useRef } from 'react'
import { Client, IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export type SeatEvent = {
  instanceId: string
  seatCode: string
  eventType: 'HELD' | 'RELEASED' | 'BOOKED'
  holdToken?: string
  expiresAt?: string
  userId?: string
  bookingId?: string
}

// instanceId: TripInstance id
// token: optional JWT to send in connect headers (if your server reads it)
// onEvent: callback invoked for each SeatEvent
export default function useSeatSocket(instanceId: string | undefined, token: string | null, onEvent: (ev: SeatEvent) => void) {
  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
    if (!instanceId) return
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      debug: (str: string) => { /* console.debug(str) */ }
    })

    client.onConnect = () => {
      client.subscribe(`/topic/trips/${instanceId}/seats`, (msg: IMessage) => {
        try {
          const ev: SeatEvent = JSON.parse(msg.body)
          onEvent(ev)
        } catch (e) {
          // ignore
        }
      })
    }

    client.onStompError = (frame) => {
      console.error('STOMP error', frame)
    }

    client.activate()
    clientRef.current = client

    return () => {
      try { client.deactivate() } catch (e) {}
      clientRef.current = null
    }
  }, [instanceId, token, onEvent])
}
