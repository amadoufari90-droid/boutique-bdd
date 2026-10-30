# Restauration PostgreSQL : .\scripts\restore.ps1 backups\boutique-XXXX.dump
param([Parameter(Mandatory = $true)][string]$Fichier)
$cfg = @{}
Get-Content .env | Where-Object { $_ -match '^[A-Z_]+=' } | ForEach-Object {
    $k, $v = $_ -split '=', 2
    $cfg[$k] = $v
}
docker cp $Fichier boutique-postgres:/tmp/restore.dump
docker exec boutique-postgres pg_restore -U $cfg["POSTGRES_USER"] -d $cfg["POSTGRES_DB"] --clean --if-exists /tmp/restore.dump
Write-Host "Restauration terminee depuis $Fichier"
