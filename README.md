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
| Vercel | ホスティング |

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

## 📁 ディレクトリ構成

```
garden-app-web/src/
├── api/                   # API呼び出し関数
│   ├── axios.js           # axiosベース設定
│   ├── auth.js            # 認証API
│   ├── plants.js          # 植物API
│   └── careLogs.js        # ケア記録API
├── components/            # 共通コンポーネント
│   ├── CareHeatmap.vue    # ヒートマップ
│   └── StreakCounter.vue  # ストリーク表示
├── stores/                # Pinia状態管理
│   └── auth.js            # 認証ストア
├── views/                 # ページコンポーネント
│   ├── LoginView.vue
│   ├── RegisterView.vue
│   ├── PlantListView.vue
│   └── PlantDetailView.vue
└── router/                # ルーティング
    └── index.js
```

## 🚀 ローカル起動手順

### バックエンド

```bash
cd garden-app-api

# application.local.yaml を作成して接続情報を設定
# spring.datasource.url / username / password
# jwt.secret

./mvnw spring-boot:run
```

### フロントエンド

```bash
cd garden-app-web

# 依存関係インストール
npm install

# 環境変数を設定
cp .env.example .env.local
# VITE_API_URL=http://localhost:8081

# 開発サーバー起動
npm run dev
```

## 🏗 アーキテクチャ

```
ユーザー
  ↓
Vercel（Vue.js / SPA）
  ↓ REST API / JWT
Render（Spring Boot）
  ↓
Supabase（PostgreSQL）
```

## 💡 技術選定の理由

- **Vue.js**：ReactはダーツシミュレータでReact済みのため、フレームワークの使い分けをアピール
- **Spring Boot**：Javaでの本格的なバックエンド設計力をアピール
- **JWT**：フロントとバックが別ドメインのSPA構成に最適
- **Pinia**：Vue公式の状態管理ライブラリ、シンプルで学習コストが低い
- **Axios**：JWTトークンをインターセプターで自動付与できるため採用
- **Supabase**：PostgreSQLの永続化を無料枠で実現
- **GitHub Actions**：CI/CDとスリープ防止pingを自動化
- **Vercel**：Vue.jsのデプロイに最適、GitHubと連携して自動デプロイ
