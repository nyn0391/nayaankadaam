@@
 export async function getSeats(instanceId: string, accessToken?: string) {
-  const inst = createAxiosInstance(accessToken)
-  const res = await inst.get(`/trips/${instanceId}/seats`)
-  return res.data as Seat[]
+  const inst = createAxiosInstance(accessToken)
+  const res = await inst.get(`/trips/${instanceId}/seatmap`)
+  return res.data as { layout: any, seats: Seat[] }
 }
