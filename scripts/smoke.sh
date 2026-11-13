#!/usr/bin/env bash
set -euo pipefail
BASE="${1:-http://localhost:8080}"

register=$(curl -sS -X POST "$BASE/auth/register" \
  -H 'Content-Type: application/json' \
  -d '{"email":"smoke@example.com","password":"password1"}')
token=$(python3 -c "import json,sys; print(json.loads(sys.argv[1])['token'])" "$register")

created=$(curl -sS -X POST "$BASE/api/codes" \
  -H "Authorization: Bearer $token" \
  -H 'Content-Type: application/json' \
  -d '{"longUrl":"https://example.com/smoke"}')
code=$(python3 -c "import json,sys; print(json.loads(sys.argv[1])['code'])" "$created")

status=$(curl -sS -o /dev/null -w '%{http_code}' "$BASE/$code")
test "$status" = "302"
echo "smoke ok code=$code"
