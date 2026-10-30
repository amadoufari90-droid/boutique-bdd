# Sauvegarde PostgreSQL (pg_dump, format custom) et Redis (snapshot RDB)
$cfg = @{}
Get-Content .env | Where-Object { $_ -match '^[A-Z_]+=' } | ForEach-Object {
    $k, $v = $_ -split '=', 2
    $cfg[$k] = $v
}
$ts = Get-Date -Format "yyyyMMdd-HHmmss"
New-Item -ItemType Directory -Force backups | Out-Null

docker exec boutique-postgres pg_dump -U $cfg["POSTGRES_USER"] -d $cfg["POSTGRES_DB"] -Fc -f /tmp/boutique.dump
docker cp boutique-postgres:/tmp/boutique.dump "backups\boutique-$ts.dump"

docker exec boutique-redis redis-cli --no-auth-warning -a $cfg["REDIS_PASSWORD"] SAVE
docker cp boutique-redis:/data/dump.rdb "backups\redis-$ts.rdb"

Write-Host "Sauvegardes creees dans backups\ ($ts)"
