package com.example.review.inventory;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/inventory")
public class SimpleInventoryApp {

    static class Item {
        Long id; String sku; String name; String description;
        Item(Long id, String sku, String name, String description) {
            this.id=id; this.sku=sku; this.name=name; this.description=description;
        }
    }
    static class Store {
        Long id; String name; String address;
        Store(Long id, String name, String address) {
            this.id=id; this.name=name; this.address=address;
        }
    }
    static class Inventory {
        Long id; Long itemId; Long storeId; int quantity; Instant updatedAt;
        Inventory(Long id, Long itemId, Long storeId, int quantity, Instant updatedAt){
            this.id=id; this.itemId=itemId; this.storeId=storeId;
            this.quantity=quantity; this.updatedAt=updatedAt;
        }
    }

    private static final Map<Long, Item> ITEMS = new LinkedHashMap<>();
    private static final Map<Long, Store> STORES = new LinkedHashMap<>();
    private static final List<Inventory> INVENTORIES = new ArrayList<>();
    static {
        // 상품
        ITEMS.put(1L, new Item(1L,"CABB-01","양배추 1통","국내산 양배추"));
        ITEMS.put(2L, new Item(2L,"LEEK-01","대파 1단","신선한 대파"));
        ITEMS.put(3L, new Item(3L,"LETT-200","상추 200g","쌈용 상추"));
        ITEMS.put(4L, new Item(4L,"TOMA-500","방울토마토 500g","달콤한 방울토마토"));
        ITEMS.put(5L, new Item(5L,"RAD-01","무 1개","국내산 무"));
        // 지점
        STORES.put(1L, new Store(1L,"싱싱채소가게","○○시장 A구역 12번"));
        STORES.put(2L, new Store(2L,"알뜰과일","○○시장 B구역 3번"));
        STORES.put(3L, new Store(3L,"시장입구야채","○○시장 입구 좌판 5"));
        STORES.put(4L, new Store(4L,"할머니채소","○○시장 C구역 9번"));
        STORES.put(5L, new Store(5L,"청과 1호","○○시장 중앙로 18"));
        // 재고
        INVENTORIES.add(new Inventory(1L, 1L, 1L, 7,  Instant.now()));
        INVENTORIES.add(new Inventory(2L, 1L, 3L, 2,  Instant.now()));
        INVENTORIES.add(new Inventory(3L, 1L, 4L, 0,  Instant.now()));
        INVENTORIES.add(new Inventory(4L, 2L, 1L, 15, Instant.now()));
        INVENTORIES.add(new Inventory(5L, 2L, 4L, 6,  Instant.now()));
        INVENTORIES.add(new Inventory(6L, 3L, 1L, 10, Instant.now()));
        INVENTORIES.add(new Inventory(7L, 3L, 5L, 4,  Instant.now()));
        INVENTORIES.add(new Inventory(8L, 4L, 2L, 12, Instant.now()));
        INVENTORIES.add(new Inventory(9L, 4L, 5L, 5,  Instant.now()));
        INVENTORIES.add(new Inventory(10L, 5L, 3L, 3, Instant.now()));
        INVENTORIES.add(new Inventory(11L, 5L, 1L, 0, Instant.now()));
    }

    @GetMapping(value = {"", "/"}, produces = MediaType.TEXT_HTML_VALUE)
    public String page() {
        return """
<!doctype html>
<html lang="ko">
<head>
  <meta charset="utf-8">
  <title>물품 재고 검색</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <style>
    html, body { font-family: system-ui, Arial, sans-serif; margin: 0; padding: 0; background:#f6f7f9; }
    .wrap { max-width: 760px; margin: 40px auto; background: #fff; padding: 20px 24px; border-radius: 12px; box-shadow: 0 6px 20px rgba(0,0,0,0.06);}
    h1 { margin: 0 0 16px; font-size: 20px; }
    .search-row { display:flex; gap:8px; }
    input[type="text"] { flex:1; padding: 12px 14px; font-size: 16px; border:1px solid #d6d6d6; border-radius: 8px; }
    button { padding: 12px 16px; font-size: 16px; border: 0; background: #2d6cdf; color:#fff; border-radius: 8px; cursor:pointer; }
    button:disabled { opacity: .6; cursor: default; }
    .tip { color:#666; font-size: 12px; margin-top:8px; }
    .result { margin-top: 18px; }
    .card { background:#fafbff; border:1px solid #e9ecf5; border-radius:10px; padding:12px 14px; margin:10px 0; }
    .item-title { font-weight:600; }
    .badge { display:inline-block; padding:2px 8px; border-radius:999px; font-size:12px; margin-left:6px; }
    .ok { background:#e6f6ec; color:#217a37; border:1px solid #bfe7cd; }
    .no { background:#fdecec; color:#b42318; border:1px solid #fac5c5; }
    table { width:100%; border-collapse: collapse; margin-top:8px; }
    th, td { border-top:1px solid #eee; padding:8px; text-align:left; font-size:14px; }
    th { background:#f7f8fb; }
    .muted { color:#777; font-size:12px; }
    .nav { margin-bottom: 16px; }
    .nav a { color:#2d6cdf; text-decoration:none; }
  </style>
</head>
<body>
  <div class="wrap">
    <div class="nav"><a href="/home">← 메인으로</a></div>
    <h1>물품 재고 검색</h1>
    <div class="search-row">
      <input id="q" type="text" placeholder="예) 무, 양배추, TOMA-500">
      <button id="btn">검색</button>
    </div>
    <div class="tip">상품명 또는 SKU 일부를 입력하고 검색하세요.</div>
    <div id="result" class="result"></div>
  </div>

  <script>
    const input = document.getElementById('q');
    const btn = document.getElementById('btn');
    const result = document.getElementById('result');

    function render(data){
      if(!data || data.count === 0){
        result.innerHTML = '<div class="muted">검색 결과가 없습니다.</div>';
        return;
      }
      const html = data.results.map(r => {
        const badge = r.available ? '<span class="badge ok">재고 있음</span>' : '<span class="badge no">재고 없음</span>';
        const rows = r.locations.map(loc => `
          <tr>
            <td>${escapeHtml(loc.storeName)}</td>
            <td>${escapeHtml(loc.address || '')}</td>
            <td>${loc.quantity}</td>
          </tr>
        `).join('');
        return `
          <div class="card">
            <div class="item-title">${escapeHtml(r.name)} <span class="muted">(${escapeHtml(r.sku)})</span> ${badge}</div>
            <div class="muted">설명: ${escapeHtml(r.description || '')}</div>
            <div class="muted">총 재고: <b>${r.totalQuantity}</b>개</div>
            <table>
              <thead><tr><th>판매처</th><th>주소</th><th>남은 수량</th></tr></thead>
              <tbody>${rows}</tbody>
            </table>
          </div>
        `;
      }).join('');
      result.innerHTML = html;
    }

    function escapeHtml(s){
      return String(s).replace(/[&<>"']/g, m => ({
        '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'
      }[m]));
    }

    async function search(){
      const q = input.value.trim();
      if(!q){ result.innerHTML = '<div class="muted">검색어를 입력하세요.</div>'; return; }
      btn.disabled = true;
      result.innerHTML = '<div class="muted">검색 중...</div>';
      try{
        const res = await fetch('/inventory/search?q=' + encodeURIComponent(q));
        const data = await res.json();
        render(data);
      }catch(e){
        result.innerHTML = '<div class="muted">오류가 발생했습니다.</div>';
      }finally{
        btn.disabled = false;
      }
    }

    btn.addEventListener('click', search);
    input.addEventListener('keydown', (e)=>{ if(e.key === 'Enter') search(); });
  </script>
</body>
</html>
""";
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> search(@RequestParam("q") String q) {
        String keyword = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        if (keyword.isEmpty()) {
            return Map.of("query", "", "results", List.of(), "count", 0);
        }

        List<Item> matched = ITEMS.values().stream()
                .filter(i -> i.name.toLowerCase(Locale.ROOT).contains(keyword)
                          || i.sku.toLowerCase(Locale.ROOT).contains(keyword))
                .limit(20)
                .collect(Collectors.toList());

        List<Map<String, Object>> results = new ArrayList<>();
        for (Item item : matched) {
            List<Inventory> invs = INVENTORIES.stream()
                    .filter(iv -> Objects.equals(iv.itemId, item.id))
                    .collect(Collectors.toList());

            int total = invs.stream().mapToInt(iv -> iv.quantity).sum();
            boolean available = total > 0;

            List<Map<String, Object>> locs = invs.stream().map(iv -> {
                Store s = STORES.get(iv.storeId);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("storeId", iv.storeId);
                row.put("storeName", s != null ? s.name : "Unknown");
                row.put("address", s != null ? s.address : "");
                row.put("quantity", iv.quantity);
                return row;
            }).toList();

            Map<String, Object> one = new LinkedHashMap<>();
            one.put("itemId", item.id);
            one.put("sku", item.sku);
            one.put("name", item.name);
            one.put("description", item.description);
            one.put("totalQuantity", total);
            one.put("available", available);
            one.put("locations", locs);
            results.add(one);
        }

        return Map.of("query", q, "results", results, "count", results.size());
    }
}
