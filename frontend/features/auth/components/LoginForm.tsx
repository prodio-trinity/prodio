"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import { useLoginMutation, useSignupMutation } from "../api/authApi";
import { queryErrorMessage } from "@/features/store/api/queryError";
import styles from "./LoginForm.module.css";

type Mode = "login" | "signup";

export function LoginForm() {
  const router = useRouter();
  const [mode, setMode] = useState<Mode>("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");

  const [login, loginState] = useLoginMutation();
  const [signup, signupState] = useSignupMutation();

  const isSubmitting = loginState.isLoading || signupState.isLoading;
  const errorMessage = loginState.isError
    ? queryErrorMessage(loginState.error, "로그인에 실패했습니다.")
    : signupState.isError
      ? queryErrorMessage(signupState.error, "회원가입에 실패했습니다.")
      : null;

  const handleModeChange = (next: Mode) => {
    setMode(next);
    loginState.reset();
    signupState.reset();
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    try {
      let roles: string[] = [];
      if (mode === "login") {
        const result = await login({ email, password }).unwrap();
        roles = result.roles;
      } else {
        const result = await signup({ email, password, name }).unwrap();
        roles = result.roles;
      }
      const destination = roles.includes("ADMIN")
        ? "/stat"
        : roles.includes("PENDING")
          ? "/pending"
          : "/my-orders";
      router.replace(destination);
    } catch {
      // 에러 메시지는 loginState/signupState.error 에서 표시한다.
    }
  };

  return (
    <div className={styles.card}>
      <div className={styles.logoArea}>
        <div className={styles.logoBadge}>P</div>
        <h1 className={styles.brand}>Prodio</h1>
      </div>

      <div className={styles.tabs} role="tablist" aria-label="로그인 방식 선택">
        <button
          type="button"
          role="tab"
          aria-current={mode === "login"}
          className={styles.tab}
          onClick={() => handleModeChange("login")}
        >
          로그인
        </button>
        <button
          type="button"
          role="tab"
          aria-current={mode === "signup"}
          className={styles.tab}
          onClick={() => handleModeChange("signup")}
        >
          회원가입
        </button>
      </div>

      <form className={styles.form} onSubmit={handleSubmit}>
        {mode === "signup" ? (
          <div className={styles.field}>
            <label className={styles.label} htmlFor="name">이름</label>
            <input
              id="name"
              type="text"
              className={styles.input}
              value={name}
              onChange={(event) => setName(event.target.value)}
              autoComplete="name"
            />
          </div>
        ) : null}

        <div className={styles.field}>
          <label className={styles.label} htmlFor="email">이메일</label>
          <input
            id="email"
            type="email"
            required
            className={styles.input}
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            autoComplete="email"
          />
        </div>

        <div className={styles.field}>
          <label className={styles.label} htmlFor="password">비밀번호</label>
          <input
            id="password"
            type="password"
            required
            className={styles.input}
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete={mode === "login" ? "current-password" : "new-password"}
          />
        </div>

        {errorMessage ? <p className={styles.error}>{errorMessage}</p> : null}

        <button type="submit" className={styles.submit} disabled={isSubmitting}>
          {isSubmitting ? "처리 중..." : mode === "login" ? "로그인" : "회원가입"}
        </button>
      </form>
    </div>
  );
}
