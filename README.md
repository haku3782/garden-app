# 🌱 家庭菜園管理アプリ

家庭菜園の植物とケア記録を管理するWebアプリです。
水やり・肥料・収穫などの記録をヒートマップとストリークで可視化します。

## 🔗 リンク

- フロントエンド：https://garden-app-web-dun.vercel.app
- バックエンドAPI：https://garden-app-odmi.onrender.com
- Swagger UI：https://garden-app-odmi.onrender.com/swagger-ui/index.html

## 🛠 技術スタック

### フロントエンド
| 技術 | 用途 |
|------|------|
| Vue.js 3 | UIフレームワーク |
| Pinia | 状態管理 |
| Vue Router | ルーティング |
| Axios | HTTP通信 |

### バックエンド
| 技術 | 用途 |
|------|------|
| Java / Spring Boot 3 | REST API |
| Spring Security | 認証・認可 |
| JWT | トークン認証 |
| Spring Data JPA | DBアクセス |
| Swagger / OpenAPI | APIドキュメント |

### インフラ
| 技術 | 用途 |
|------|------|
| Supabase | PostgreSQL |
| Render | バックエンドホスティング |
| Vercel | フロントエンドホスティング |
| GitHub Actions | CI/CD・スリープ防止 |
| Docker | コンテナ化 |

## 📦 機能一覧

- ユーザー登録・ログイン（JWT認証）
- 植物の登録・編集・削除
- ケア記録（水やり・肥料・収穫）の管理
- ヒートマップで年間ケア記録を可視化
- 連続ケア日数（ストリーク）の表示

## 🗄 DB設計

```
users      id, username, password, created_at
plants     id, user_id, name, type, planted_at, memo, created_at
care_logs  id, plant_id, care_type, cared_at, memo, created_at
```

## 🚀 ローカル起動手順

### バックエンド

```bash
# 環境変数を設定
cp .env.example .env
# .envを編集してDBの接続情報を入力

# 起動
./mvnw spring-boot:run
```

### フロントエンド

```bash
cd garden-app-web
npm install
npm run dev
```

## 🏗 アーキテクチャ

```
ユーザー
  ↓
Vercel（Vue.js）
  ↓ REST API / JWT
Render（Spring Boot）
  ↓
Supabase（PostgreSQL）
```

## 💡 技術選定の理由

- **Vue.js**：ReactはダーツシミュレータでReact済みのため、フレームワークの使い分けをアピール
- **Spring Boot**：Javaでの本格的なバックエンド設計力をアピール
- **JWT**：フロントとバックが別ドメインのSPA構成に最適
- **Supabase**：PostgreSQLの永続化を無料枠で実現
- **GitHub Actions**：CI/CDとスリープ防止pingを自動化
