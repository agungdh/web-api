.DEFAULT_GOAL := help

.PHONY: help
help: ## Tampilkan daftar perintah yang tersedia
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | \
	awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

.PHONY: db-reset
db-reset: ## Hapus volume PostgreSQL & buat ulang database
	docker compose down postgres -v
	docker compose up -d postgres
	@echo "Database sudah di-reset. Jalankan ./mvnw quarkus:dev untuk auto-migrate Flyway."
