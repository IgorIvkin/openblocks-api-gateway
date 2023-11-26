package main

import (
	"github.com/gin-gonic/gin"

	api "openblocks.ru/api-gateway/check-auth-service/api"
	app "openblocks.ru/api-gateway/check-auth-service/application"
)

func main() {
	application := app.NewApplication()

	router := gin.Default()

	router.POST("/api/v1/authentication/check", func(ctx *gin.Context) {
		api.ProcessCheckAuth(application, ctx)
	})

	router.Run("localhost:8903")
}
