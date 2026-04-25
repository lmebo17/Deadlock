import { Client, IMessage } from "@stomp/stompjs";

const WS_BASE = (process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080").replace(/^http/, "ws");

export type MatchEvent = {
  type: "QUEUE_UPDATE" | "MATCH_FOUND" | "MATCH_START" | "OPPONENT_SUBMITTED" |
        "OPPONENT_DISCONNECTED" | "OPPONENT_RECONNECTED" | "MATCH_END";
  matchId?: number;
  opponentId?: number;
  opponentUsername?: string;
  opponentAvatarUrl?: string;
  opponentElo?: number;
  problemSlug?: string;
  problemTitle?: string;
  durationSec?: number;
  startedAt?: string;
  submissionCount?: number;
  winnerId?: number | null;
  yourEloChange?: number;
  opponentEloChange?: number;
  yourFinalElo?: number;
  finalStatus?: string;
  queueSize?: number;
  waitTimeSec?: number;
};

export type MatchEventHandler = (event: MatchEvent) => void;

export class MatchWebSocket {
  private client: Client | null = null;
  private personalHandler: MatchEventHandler | null = null;
  private matchHandlers = new Map<number, MatchEventHandler>();
  private connectedResolvers: (() => void)[] = [];
  private isConnected = false;

  connect(): Promise<void> {
    if (this.isConnected) return Promise.resolve();
    return new Promise((resolve) => {
      this.connectedResolvers.push(resolve);

      if (this.client) return;

      this.client = new Client({
        brokerURL: `${WS_BASE}/ws`,
        connectHeaders: {},
        debug: () => {}, // silence
        reconnectDelay: 3000,
        heartbeatIncoming: 20000,
        heartbeatOutgoing: 20000,
        onConnect: () => {
          this.isConnected = true;
          // Personal queue subscription
          this.client!.subscribe("/user/queue/match-events", (msg: IMessage) => {
            if (this.personalHandler) {
              try { this.personalHandler(JSON.parse(msg.body)); } catch {}
            }
          });
          this.connectedResolvers.forEach((r) => r());
          this.connectedResolvers = [];
        },
        onStompError: (frame) => {
          console.error("STOMP error", frame);
        },
        onDisconnect: () => {
          this.isConnected = false;
        },
      });

      this.client.activate();
    });
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.isConnected = false;
      this.matchHandlers.clear();
      this.personalHandler = null;
    }
  }

  onPersonalEvent(handler: MatchEventHandler) {
    this.personalHandler = handler;
  }

  subscribeMatch(matchId: number, handler: MatchEventHandler) {
    if (!this.client || !this.isConnected) return;
    this.matchHandlers.set(matchId, handler);
    this.client.subscribe(`/topic/match/${matchId}`, (msg: IMessage) => {
      try { handler(JSON.parse(msg.body)); } catch {}
    });
  }

  joinQueue(timeControl: string, difficulty: string) {
    if (!this.client || !this.isConnected) return;
    this.client.publish({
      destination: "/app/queue/join",
      body: JSON.stringify({ timeControl, difficulty }),
    });
  }

  leaveQueue() {
    if (!this.client || !this.isConnected) return;
    this.client.publish({ destination: "/app/queue/leave", body: "" });
  }
}

let singleton: MatchWebSocket | null = null;

export function getMatchWebSocket(): MatchWebSocket {
  if (!singleton) singleton = new MatchWebSocket();
  return singleton;
}
