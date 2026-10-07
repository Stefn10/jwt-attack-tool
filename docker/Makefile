.PHONY: build up down logs clean demo

build:
	./build-wars.sh
	docker compose build

up:
	docker compose up -d

down:
	docker compose down

logs:
	docker compose logs -f tomcat

clean:
	docker compose down -v
	rm -rf wars/*.war

# One-shot: build WARs + images + start.
demo: build
	docker compose up -d
	@echo ""
	@echo@echo "Open: http://localhost:8080/jwt-tool/"
