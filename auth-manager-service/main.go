package main

import (
	"github.com/gin-gonic/gin"
	api "openblocks.ru/api-gateway/auth-manager-service/api"
	app "openblocks.ru/api-gateway/auth-manager-service/application"
)

func main() {
	application := app.NewApplication()

	router := gin.Default()

	router.POST("/api/v1/oauth2/token", func(ctx *gin.Context) {
		api.ProcessIssueAccessToken(application, ctx)
	})

	router.Run("127.0.0.1:8081")
}
