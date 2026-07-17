export function validateUsername(username) {
  if (!username || username.length < 3 || username.length > 30) {
    return 'ユーザー名は3〜30文字で入力してください'
  }
  return null
}

export function validatePassword(password) {
  if (!password || password.length < 8 || password.length > 72) {
    return 'パスワードは8〜72文字で入力してください'
  }
  return null
}
