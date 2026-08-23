"use client";

import { useEffect, type RefObject } from "react";

/** open이 true인 동안, ref로 감싼 영역 바깥을 클릭하면 onClose를 호출한다. */
export function useClickOutside(
  ref: RefObject<HTMLElement | null>,
  open: boolean,
  onClose: () => void,
) {
  useEffect(() => {
    if (!open) return;

    function handleClickOutside(event: MouseEvent) {
      if (!ref.current?.contains(event.target as Node)) {
        onClose();
      }
    }

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [open, ref, onClose]);
}
