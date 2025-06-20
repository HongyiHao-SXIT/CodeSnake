precision mediump float;
varying vec2 vTexCoord;
uniform sampler2D uTexture;

// 鱼眼相关参数，可根据实际情况调整，比如焦距等，这里只是示例简单模型
const float PI = 3.1415926535;
const float maxRadius = 0.5; // 假设鱼眼纹理有效半径在 0.5 范围内（纹理坐标是 0 - 1 范围）

vec2 fisheyeToPerspective(vec2 texCoord) {
    vec2 center = vec2(0.5, 0.5);
    vec2 offset = texCoord - center;
    float radius = length(offset);
    if (radius > maxRadius) {
        return texCoord; // 超出鱼眼有效范围，保持原坐标
    }
    float theta = atan(offset.y, offset.x);
    float normalizedRadius = radius / maxRadius;
    // 简单的畸变矫正映射，这里用的是一种近似方式，可根据实际镜头参数优化
    float newRadius = asin(normalizedRadius) / (PI / 2.0);
    newRadius *= maxRadius;
    return center + newRadius * vec2(cos(theta), sin(theta));
}

void main() {
   // vec2 correctedTexCoord = fisheyeToPerspective(vTexCoord);
    // gl_FragColor = texture2D(uTexture, correctedTexCoord);

     vec2 center = vec2(0.5, 0.5);
         float dist = distance(vTexCoord, center);

         // 设置圆形区域的半径
         float radius = 0.5; // 可调整此值控制圆形大小

         // 采样原始图像
         vec4 originalColor = texture2D(uTexture, vTexCoord);

         // 判断是否在圆形区域内（使用 step 函数创建硬边界）
         float insideCircle = step(dist, radius);

         // 圆形区域内显示原图，外部显示黑色
         gl_FragColor = mix(vec4(0.0, 0.0, 0.0, 1.0), originalColor, insideCircle);
}