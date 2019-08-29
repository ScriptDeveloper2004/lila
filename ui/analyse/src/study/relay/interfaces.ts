export interface RelayData {
  id: string;
  slug: string;
  description?: string;
  sync: RelaySync;
}

export interface RelaySync {
  ongoing: boolean;
  url: string;
  log: LogEvent[];
}

export interface RelayIntro {
  exists: boolean;
  active: boolean;
  disable(): void;
}

export interface LogEvent {
  id: string;
  moves: number;
  error?: string;
  at: number;
}
