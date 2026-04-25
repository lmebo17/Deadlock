"use client";

import { useRef } from "react";
import Editor, { type Monaco } from "@monaco-editor/react";
import type { editor } from "monaco-editor";

interface CodeEditorProps {
  language: string;
  value: string;
  onChange: (value: string) => void;
  onSubmit?: () => void;
  onRun?: () => void;
}

const LANGUAGE_MAP: Record<string, string> = {
  PYTHON: "python",
  JAVA: "java",
  CPP: "cpp",
};

export function CodeEditor({ language, value, onChange, onSubmit, onRun }: CodeEditorProps) {
  const submitRef = useRef(onSubmit);
  const runRef = useRef(onRun);
  submitRef.current = onSubmit;
  runRef.current = onRun;

  const handleMount = (editor: editor.IStandaloneCodeEditor, monaco: Monaco) => {
    editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter, () => {
      submitRef.current?.();
    });
    editor.addCommand(
      monaco.KeyMod.CtrlCmd | monaco.KeyMod.Shift | monaco.KeyCode.Enter,
      () => {
        runRef.current?.();
      }
    );
  };

  return (
    <div className="rounded-lg border border-border overflow-hidden">
      <Editor
        height="400px"
        language={LANGUAGE_MAP[language] || "python"}
        value={value}
        onChange={(val) => onChange(val || "")}
        theme="vs-dark"
        onMount={handleMount}
        options={{
          minimap: { enabled: false },
          fontSize: 14,
          lineNumbers: "on",
          scrollBeyondLastLine: false,
          automaticLayout: true,
          tabSize: 4,
          wordWrap: "on",
          padding: { top: 12, bottom: 12 },
        }}
      />
    </div>
  );
}
