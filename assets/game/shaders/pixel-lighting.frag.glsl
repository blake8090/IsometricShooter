#ifdef GL_ES
precision mediump float;
#endif

#define MAX_LIGHTS 16

uniform sampler2D u_texture;
uniform vec3 u_ambientLight;
uniform int u_lightCount;
uniform vec4 u_lightPositionRadii[MAX_LIGHTS];
uniform vec4 u_lightColorIntensity[MAX_LIGHTS];

varying vec2 v_position;
varying vec2 v_texCoords;
varying vec4 v_color;

void main() {
    vec3 lightColor = u_ambientLight;

    for (int i = 0; i < MAX_LIGHTS; i++) {
        if (i >= u_lightCount) {
            break;
        }

        vec4 positionRadii = u_lightPositionRadii[i];
        vec2 normalizedDelta = (v_position - positionRadii.xy) / positionRadii.zw;
        float attenuation = max(0.0, 1.0 - length(normalizedDelta));
        attenuation *= attenuation;

        vec4 colorIntensity = u_lightColorIntensity[i];
        lightColor += colorIntensity.rgb * colorIntensity.a * attenuation;
    }

    vec4 textureColor = texture2D(u_texture, v_texCoords);
    gl_FragColor = vec4(
        textureColor.rgb * v_color.rgb * min(lightColor, vec3(1.0)),
        textureColor.a * v_color.a
    );
}
