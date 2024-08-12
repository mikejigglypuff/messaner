const { createProxyMiddleware } = require('http-proxy-middleware');
const defaultURL = process.env.REACT_APP_DEFAULT_URL;

module.exports = function(app) {
  app.use(
    '/',
    createProxyMiddleware({
      target: defaultURL,	// 서버 URL or localhost:설정한포트번호
      changeOrigin: true,
    })
  );
};