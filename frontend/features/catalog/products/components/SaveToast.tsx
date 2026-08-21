"use client";

import { useEffect, useState } from "react";
import { CheckCircle2 } from "lucide-react";
import styles from "./ProductList.module.css";

interface SaveToastProps {
  message: string;
  /** message가 이전과 같은 문구여도 연달아 새로 띄우기 위한 트리거 값. */
  toastId: number;
}

const DISPLAY_MS = 3000;

export function SaveToast({ message, toastId }: SaveToastProps) {
  const [hiddenId, setHiddenId] = useState<number | null>(null);

  useEffect(() => {
    if (!message) return;
    const timer = window.setTimeout(() => setHiddenId(toastId), DISPLAY_MS);
    return () => window.clearTimeout(timer);
  }, [message, toastId]);

  const visible = Boolean(message) && hiddenId !== toastId;
  if (!visible) return null;

  return (
    <div key={toastId} className={styles.toast}>
      <CheckCircle2 size={18} />
      <span>{message}</span>
    </div>
  );
}
