package api

import (
	"fmt"
	"math"
	"net/http"
	"os"
	"time"

	"github.com/gin-gonic/gin"
	app "openblocks.ru/api-gateway/auth-manager-service/application"
	client "openblocks.ru/api-gateway/auth-manager-service/client"
	service "openblocks.ru/api-gateway/auth-manager-service/service"
)

type TokenIssueRequest struct {
	GrantType    string `form:"grant_type" binding:"required"`
	ClientId     string `form:"client_id" binding:"required"`
	ClientSecret string `form:"client_secret" binding:"required"`
}

type TokenRefreshRequest struct {
	AccessToken string `form:"access_token" binding:"required"`
}

// Process issue of access token.
func ProcessIssueAccessToken(application *app.Application, ctx *gin.Context) {

	var request TokenIssueRequest
	err := ctx.ShouldBind(&request)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Cannot bind request to process issue access token: %v\n", err)
		ctx.JSON(http.StatusBadRequest, gin.H{
			"message": "Incorrect set of required parameters provided",
		})
		return
	}

	// publicKey, err := jwt.ParseECPublicKeyFromPEM([]byte(application.Config.Signing.PublicKey))
	// if err != nil {
	// 	fmt.Fprintf(os.Stderr, "Cannot parse public key: %v\n", err)
	// 	os.Exit(1)
	// }

	clientVerified, err := client.CheckAuth(application, request.ClientId, request.ClientSecret)
	if clientVerified && err == nil {

		// verifiedToken, err := jwt.Parse(signedAccessToken, func(token *jwt.Token) (interface{}, error) {
		// 	return publicKey, nil
		// })

		expiresAt := time.Now().Add(30 * time.Minute)
		expiredAtInSeconds := math.Round(expiresAt.Sub(time.Now()).Seconds())

		accessToken, err := service.IssueAccessToken(application, expiresAt)
		if err != nil {
			fmt.Fprintf(os.Stderr, "Cannot bind request to process issue access token: %v\n", err)
			ctx.JSON(http.StatusInternalServerError, gin.H{
				"message": "Internal error, see log for details",
			})
			return
		}

		// TODO store acess token here

		ctx.JSON(http.StatusOK, gin.H{
			"access_token": accessToken,
			"token_type":   "Bearer",
			"expires_in":   expiredAtInSeconds,
		})
	} else if err == nil {
		ctx.JSON(http.StatusForbidden, gin.H{
			"message": "Wrong client_id or client_secret provided",
		})
	} else {
		ctx.JSON(http.StatusServiceUnavailable, gin.H{
			"message": "Something went wrong on checking client authentication",
		})
	}
}

func ProcessRefreshAccessToken(application *app.Application, ctx *gin.Context) {

	var request TokenRefreshRequest
	err := ctx.ShouldBind(&request)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Cannot bind request to process refresh access token: %v\n", err)
		ctx.JSON(http.StatusBadRequest, gin.H{
			"message": "Incorrect set of required parameters provided",
		})
		return
	}

	// TODO validate token was really issued and expired not older than 1 day ago
}
