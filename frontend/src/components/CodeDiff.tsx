"use client";

import { DiffEditor } from "@monaco-editor/react";

interface CodeDiffProps {
  language: string;
  original: string;
  modified: string;
  originalLabel?: string;
  modifiedLabel?: string;
}

const LANGUAGE_MAP: Record<string, string> = {
  PYTHON: "python",
  JAVA: "java",
  CPP: "cpp",
};

export function CodeDiff({ language, original, modified, originalLabel, modifiedLabel }: CodeDiffProps) {
  return (
    <div className="rounded-lg border border-border overflow-hidden">
      {(originalLabel || modifiedLabel) && (
        <div className="grid grid-cols-2 border-b border-border bg-secondary/50 text-xs font-medium">
          <div className="px-3 py-2 border-r border-border">{originalLabel ?? ""}</div>
          <div className="px-3 py-2">{modifiedLabel ?? ""}</div>
        </div>
      )}
      <DiffEditor
        height="500px"
        language={LANGUAGE_MAP[language] || "plaintext"}
        original={original}
        modified={modified}
        theme="vs-dark"
        options={{
          minimap: { enabled: false },
          fontSize: 13,
          lineNumbers: "on",
          renderSideBySide: true,
          readOnly: true,
          scrollBeyondLastLine: false,
          automaticLayout: true,
          wordWrap: "on",
        }}
      />
    </div>
  );
}
