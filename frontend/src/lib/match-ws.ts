import { Client, IMessage } from "@stomp/stompjs";

const WS_BASE = (process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080").replace(/^http/, "ws");

export type WSConnectionState = "DISCONNECTED" | "CONNECTING" | "CONNECTED" | "RECONNECTING";

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
export type StateHandler = (state: WSConnectionState) => void;

export class MatchWebSocket {
  private client: Client | null = null;
  private personalHandler: MatchEventHandler | null = null;
  private matchHandlers = new Map<number, MatchEventHandler>();
  private connectedResolvers: (() => void)[] = [];
  private state: WSConnectionState = "DISCONNECTED";
  private stateListeners = new Set<StateHandler>();
  private hasConnectedBefore = false;

  getState(): WSConnectionState {
    return this.state;
  }

  onStateChange(listener: StateHandler): () => void {
    this.stateListeners.add(listener);
    return () => this.stateListeners.delete(listener);
  }

  private setState(state: WSConnectionState) {
    if (this.state === state) return;
    this.state = state;
    this.stateListeners.forEach((l) => l(state));
  }

  connect(): Promise<void> {
    if (this.state === "CONNECTED") return Promise.resolve();
    return new Promise((resolve) => {
      this.connectedResolvers.push(resolve);

      if (this.client) return;

      this.setState("CONNECTING");
      this.client = new Client({
        brokerURL: `${WS_BASE}/ws`,
        connectHeaders: {},
        debug: () => {},
        reconnectDelay: 3000,
        heartbeatIncoming: 20000,
        heartbeatOutgoing: 20000,
        onConnect: () => {
          const wasReconnect = this.hasConnectedBefore;
          this.hasConnectedBefore = true;
          this.setState("CONNECTED");
          this.client!.subscribe("/user/queue/match-events", (msg: IMessage) => {
            if (this.personalHandler) {
              try { this.personalHandler(JSON.parse(msg.body)); } catch {}
            }
          });
          // Resubscribe match topics on reconnect
          if (wasReconnect) {
            this.matchHandlers.forEach((handler, matchId) => {
              this.client!.subscribe(`/topic/match/${matchId}`, (msg: IMessage) => {
                try { handler(JSON.parse(msg.body)); } catch {}
              });
            });
          }
          this.connectedResolvers.forEach((r) => r());
          this.connectedResolvers = [];
        },
        onStompError: (frame) => {
          console.error("STOMP error", frame);
        },
        onWebSocketClose: () => {
          if (this.hasConnectedBefore) {
            this.setState("RECONNECTING");
          } else {
            this.setState("DISCONNECTED");
          }
        },
        onDisconnect: () => {
          this.setState("DISCONNECTED");
        },
      });

      this.client.activate();
    });
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.matchHandlers.clear();
      this.personalHandler = null;
      this.hasConnectedBefore = false;
      this.setState("DISCONNECTED");
    }
  }

  onPersonalEvent(handler: MatchEventHandler) {
    this.personalHandler = handler;
  }

  subscribeMatch(matchId: number, handler: MatchEventHandler) {
    if (!this.client || this.state !== "CONNECTED") return;
    this.matchHandlers.set(matchId, handler);
    this.client.subscribe(`/topic/match/${matchId}`, (msg: IMessage) => {
      try { handler(JSON.parse(msg.body)); } catch {}
    });
  }

  joinQueue(timeControl: string, difficulty: string) {
    if (!this.client || this.state !== "CONNECTED") return;
    this.client.publish({
      destination: "/app/queue/join",
      body: JSON.stringify({ timeControl, difficulty }),
    });
  }

  leaveQueue() {
    if (!this.client || this.state !== "CONNECTED") return;
    this.client.publish({ destination: "/app/queue/leave", body: "" });
  }
}

let singleton: MatchWebSocket | null = null;

export function getMatchWebSocket(): MatchWebSocket {
  if (!singleton) singleton = new MatchWebSocket();
  return singleton;
}
