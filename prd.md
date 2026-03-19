ビュオリンゴ！

View Onlyのフォトアルバムアプリ。

子供に操作されるのがウザいので。他にも用途が出るかもだが。

できるだけアプリを閉じれないモードを設ける。

グローバル展開前提

---

# ビュオリン子 / Vuorinko

**Product Requirements Document**

|  |  |
| --- | --- |
| **Version** | 1.0 (v1 MVP) |
| **Date** | 2026-03-14 |
| **Status** | Draft |
| **Brand Family** | komakoma シリーズ |

---

## 1. Product Overview

### 1.1 One-liner

ビュオリン子は、「見るだけ」に特化した完全オフラインのフォトアルバムアプリ。子供に端末を渡しても、写真を削除・共有・編集される心配がない。

### 1.2 Background & Motivation

幼児にスマートフォンを渡して写真を見せたい場面は日常的に発生する。しかし標準の写真アプリでは、子供が誤って写真を削除・共有したり、アプリを閉じて他の操作をしてしまう。OSのガイドアクセスやピン留め機能は存在するが、設定が煩雑でフォトアルバム体験として設計されていない。

本アプリは収益化を目的とせず、komakomaブランドのソフトウェア開発哲学を体現する製品として位置づける。

### 1.3 Brand Philosophy (komakoma共通)

- **Privacy First** — データは端末内に完結。外部通信ゆるぎなし。
- **Family Oriented** — 子供と家族の日常を豊かにする。
- **Minimal & Intentional** — 機能は最小限、体験は最大限。
- **Platform Native** — 各OSのFirst Party APIを最大限活用。
- **Global First** — 初日から多言語・多地域対応。

### 1.4 Naming

- 日本語表記: **ビュオリン子**（「子」を「ゴ」と読ませる当て読み）
- グローバル表記: **Vuorinko**
- App Storeサブタイトル: View-Only Photo Album

---

## 2. Target Users

### 2.1 Primary

スマートフォンで子供に写真を見せたい親（幼児〜小学校低学年）

### 2.2 Secondary

- 店舗ディスプレイ用途（商品写真の展示）
- 介護施設での写真閲覧
- 展示会・イベントでのポートフォリオ表示

---

## 3. Functional Requirements (v1)

### 3.1 Parent Mode（設定側）

| ID | Feature | Detail |
| --- | --- | --- |
| P-01 | アルバム作成 | 複数アルバム作成可（名前付き、最大20アルバム、名前は50文字以内） |
| P-02 | 写真選択 | 端末の写真ライブラリから選択してアルバムに追加（1アルバムあたり最大500枚） |
| P-03 | 写真削除・並べ替え | アルバム内の写真を管理 |
| P-04 | 認証ロック | PINまたは生体認証で親モードを保護 |
| P-05 | 子供モード切り替え | アルバム1つを選択→子供モードへの遷移（子供モード中のアルバム切り替えは不可） |

### 3.2 Child Mode（閲覧側）

| ID | Feature | Detail |
| --- | --- | --- |
| C-01 | スワイプ閲覧 | 1枚ずつ左右スワイプで写真を閲覧 |
| C-02 | 操作制限 | 削除・共有・編集・ズーム全て無効 |
| C-03 | マルチタッチ抑制 | 5本指以上の同時タッチを無視（掌ベタ置き対策） |
| C-04 | 親モード復帰 | PIN/生体認証でのみ親モードに戻れる |

### 3.3 PIN認証仕様

| 項目 | 仕様 |
| --- | --- |
| PIN形式 | 4桁数字固定 |
| 保存方式 | SHA-256ハッシュ + ソルト。iOS: Keychain、Android: EncryptedSharedPreferences |
| 試行制限 | 5回連続失敗で30秒ロックアウト。以降も5回ごとに30秒ロック |
| 生体認証 | 端末に設定済みの場合、優先表示。失敗/キャンセルでPIN入力にフォールバック |
| PIN忘れ時 | アプリの全データ初期化（アルバムデータ削除）のみ。写真ライブラリには影響なし |
| 初期設定 | 初回起動時に必須設定 |

### 3.4 写真保存方式

参照方式を採用する。アプリはフォトライブラリの識別子のみを保持し、写真データのコピーは行わない。

| Platform | 参照方式 |
| --- | --- |
| iOS | PHAsset localIdentifier |
| Android | MediaStore content URI |

**参照が無効になった場合（元写真が削除された等）:**
- プレースホルダー画像を表示（「この写真は利用できません」）
- 親モードで該当写真の削除を促すバッジ表示

### 3.5 初回起動フロー

1. ようこそ画面（アプリ概要説明）
2. PIN設定（4桁数字 × 確認入力）
3. 写真アクセス権限の要求（フルアクセスを推奨。Limited Accessの場合は縮退動作）
4. Guided Access / Screen Pinning ガイド（スキップ可能）
5. アルバム作成を促すホーム画面

**iOS Limited Photo Access時の縮退動作:**
- PHPicker経由での写真選択のみ対応
- アルバム一覧からの動的参照は無効
- フルアクセスへの変更を促すバナーを親モードに表示

### 3.6 Exit Prevention（アプリ終了抑止）

独自のロック機構は実装せず、OS機能との連携で実現する。**完全な終了防止はOS制約上不可能であり、ベストエフォート対策として位置づける。**

**iOS:**

- `prefersHomeIndicatorAutoHidden` — ホームインジケーターを自動的に非表示
- `preferredScreenEdgesDeferringSystemGestures` — 上下エッジスワイプを1回目無視（2回目で突破可能）
- Guided Access誘導 — オンボーディングで設定方法をスクリーンショット付きでガイド（Guided Accessはユーザーが手動で開始する必要がある）

**Android:**

- `WindowInsetsControllerCompat` + `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE` — フルスクリーン化（API 30+対応。API 29は`SYSTEM_UI_FLAG_IMMERSIVE_STICKY`にフォールバック）
- Screen Pinning誘導 — オンボーディングで画面の固定設定をガイド（`startLockTask()`呼び出し時にシステム確認ダイアログが表示される）

**制限事項（App Store説明文・オンボーディングで明示）:**

- Guided Access / Screen Pinning を使用しない場合、子供がアプリを閉じる可能性がある
- 着信等のシステム割り込みではアプリが中断される

---

## 4. Non-Functional Requirements

| Category | Requirement |
| --- | --- |
| **Network** | 完全オフライン動作。ネットワーク権限を一切要求しない。 |
| **Photo Access** | フルフォトライブラリアクセス。親モードでの写真選択に必要。 |
| **Privacy** | App Privacy Label:「データ収集なし」。ATT不要。Analytics SDK不使用。 |
| **Localization** | 初版から日本語・英語対応。ローカライズ基盤を初期設計に含む。 |
| **Platform** | iOS 17+ / Android 10+ 同時リリース |
| **Monetization** | なし。完全無料。広告なし。 |

---

## 5. Technical Architecture

### 5.1 Cross-Platform Strategy: KMP

Kotlin Multiplatform (KMP) を採用。Compose Multiplatform (CMP) ではない。

**判断理由:**

- UIがシンプル（スワイプビューワー＋設定画面）なのでUI共通化のメリットが薄い
- OS固有APIとの深い統合（エッジスワイプ制御、イマーシブモード等）がアプリの核心価値
- CMPだとiOS側でCompose UIの上からUIKitを制御する不自然なレイヤーが発生
- 「各プラットフォームを深く理解している」というブランドメッセージと合致

### 5.2 Module Structure

| Module | Language | Responsibility |
| --- | --- | --- |
| `:shared` | Kotlin (Common) | ビジネスロジック（アルバムCRUDルール、PIN検証）、Repositoryインターフェース定義 |
| `:app-ios` | Swift / SwiftUI | UI + PhotoKit + エッジジェスチャー制御 |
| `:app-android` | Kotlin / Compose | UI + MediaStore + イマーシブモード |

### 5.3 Key Technical Decisions

| Topic | iOS | Android |
| --- | --- | --- |
| Photo Access | PhotoKit (PHAsset) | MediaStore API |
| UI Framework | SwiftUI | Jetpack Compose |
| Local Storage | SQLDelight (KMP共通) | SQLDelight (KMP共通) |
| Auth | LocalAuthentication | BiometricPrompt |
| Exit Prevention | UIViewController override | WindowInsetsControllerCompat + Screen Pinning |

---

## 6. Edge Cases & Behavior

| ケース | 動作 |
| --- | --- |
| 電源OFF/再起動後の起動 | 常に親モード（PIN入力画面）で起動 |
| 空アルバムで子供モード遷移 | 遷移不可。「写真を追加してください」メッセージ表示 |
| アルバム内写真が端末から削除された | プレースホルダー表示。親モードで削除を促す |
| 子供モード中に着信 | OS割り込みを許容（制御不可）。通話終了後アプリに復帰 |
| 写真アクセス権限の事後変更（フル→制限） | 親モードでフルアクセスへの変更を促すバナー表示 |
| 生体認証の事後無効化 | PIN入力のみにフォールバック |
| ダークモード切り替え | 両モードに対応。子供モードは白背景固定も検討 |
| 同一写真の複数アルバム追加 | 許可（参照のみのためストレージ影響なし） |

---

## 7. App Store Strategy

### 7.1 Review Considerations

- **NSPhotoLibraryUsageDescription** — 「お子様に見せたい写真をアルバムに追加するために、フォトライブラリへのアクセスが必要です。」
- **App Review Note** — 「このアプリはネットワーク権限を一切使用しません。写真データは端末内の参照のみで外部送信は行いません。」
- **Info.plist** — ネットワーク関連の権限を一切含めない
- **App Privacy Label** — 「データ収集なし」で提出
- 独自ロック機構を作らず、OS機能への誘導に徹めることで審査リスクを回避

### 7.2 ASO (App Store Optimization)

- キーワード: view only, kids photo, child safe, photo album, toddler proof
- 多言語メタデータを初版から投入
- スクリーンショット: 親モードと子供モードの対比を視覚的に見せる

---

## 8. Scope Definition

### 8.1 v1に含むもの

- アルバム作成・管理（複数対応）
- 写真ライブラリからの写真選択・追加
- スワイプ閲覧（View Only）
- 操作制限（削除・共有・編集・ズーム無効）
- マルチタッチ抑制
- PIN / 生体認証ロック
- エッジジェスチャー制御（iOS / Android）
- OSキオスクモードへの誘導オンボーディング
- 完全オフライン動作
- 日本語 / 英語ローカライズ

### 8.2 v1で明確に切るもの

- 動画対応
- クラウド同期
- 家族間共有
- アプリ内カメラ
- ウィジェット
- 課金機能
- Analytics / Crash Reporting SDK

---

## 9. Success Metrics

収益化を目的としないため、ブランディング観点での指標を設定する。

1. App Storeレビュー評価 4.5以上
2. GitHubリポジトリのスター数（開発哲学の認知）
3. カンファレンス登壇・ブログ記事化の実績
4. komakomaブランドとしての被言及数

---

*komakoma © 2026 — Privacy First, Family Oriented, Minimal & Intentional*