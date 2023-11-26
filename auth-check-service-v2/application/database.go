package application

import (
	"context"
	"fmt"
	"os"

	"github.com/jackc/pgx/v5/pgxpool"
)

func GetConnection(application *Application) *pgxpool.Pool {

	var databaseUrl string = fmt.Sprintf(
		"postgres://%s:%s@%s?pool_max_conns=%d",
		application.Config.Database.DatabaseUser,
		application.Config.Database.DatabasePassword,
		application.Config.Database.DatabaseUrl,
		application.Config.Database.MaxPoolSize)

	dbpool, err := pgxpool.New(context.Background(), databaseUrl)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Unable to create connection pool: %v\n", err)
		os.Exit(1)
	}

	return dbpool
}
