package application

import (
	"context"
	"fmt"
	"os"

	app "openblocks.ru/api-gateway/check-auth-service/application"
)

type UserData struct {
	Id       int64
	Password string
}

type UserDataRepository struct {
	Application *app.Application
}

// Возвращает данные по пользователю по заданному логину пользователя.
func (rep UserDataRepository) GetByLogin(login string) (*UserData, error) {
	dbpool := app.GetConnection(rep.Application)
	defer dbpool.Close()

	var userData UserData
	err := dbpool.QueryRow(context.Background(), "select id, password from user_data ud where ud.login = $1", login).Scan(
		&userData.Id,
		&userData.Password)
	if err != nil {
		fmt.Fprintf(os.Stderr, "QueryRow failed to get user by login: %v\n", err)
		return nil, err
	}

	return &userData, nil
}
