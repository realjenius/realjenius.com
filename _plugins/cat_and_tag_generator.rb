module Jekyll

  class CatsAndTags < Generator
  
    safe true

    def generate(site)
      site.categories.each do |category|
        build(site, "category", category)
      end

      site.tags.each do |tag|
        build(site, "tag", tag)
      end
    end

    def build(site, thing_type, thing) 
      thing[1] = thing[1].sort_by { |p| -p.date.to_f }     
      atomize(site, thing_type, thing)
      paginate(site, thing_type, thing)
    end

    def atomize(site, type, thing)
      path = "/#{type}/#{thing[0]}"
      atom = AtomPage.new(site, site.source, path, type, thing[0])
      site.pages << atom
    end

    def paginate(site, type, posts)
      pages = Pager.calculate_pages(posts[1], site.config['paginate'].to_i)
      (1..pages).each do |num_page|
        pager = GroupPager.new(site.config, num_page, posts[1], type, posts[0], pages)
        path = "/#{type}/#{posts[0]}"
        if num_page > 1
          
          new_path = path + "/page#{num_page}"
          newpage = GroupSubPage.new(site, site.source, new_path, type, posts[0])
          newpage.pager = pager
          site.pages << newpage
        else
          newpage = GroupSubPage.new(site, site.source, path, type, posts[0])
          newpage.pager = pager
          site.pages << newpage
        end

      end
    end

  end

  class GroupPager < Pager

    attr_reader :category, :type

    # same as the base class, but includes the category value
    def initialize(config, page, all_posts, category, type, num_pages = nil)
    	@category = category
      @type = type
      super config, page, all_posts, num_pages
    end

    # use the original to_liquid method, but add in category info
    alias_method :original_to_liquid, :to_liquid
    def to_liquid
      x = original_to_liquid
      x[@type] = @category
      x
    end
  end

  class GroupSubPage < Page
    def initialize(site, base, dir, type, val)
      @site = site
      @base = base
      @dir = dir
      @name = 'index.html'

      self.process(@name)
      self.read_yaml(File.join(base, '_layouts'), "#{type}_index.html")
      self.data[type] = val
    end
  end
  
  class AtomPage < Page
    def initialize(site, base, dir, type, val)
      @site = site
      @base = base
      @dir = dir
      @name = 'atom.xml'

      self.process(@name)
      self.read_yaml(File.join(base, '_layouts'), "#{type}_atom.xml")
      self.data[type] = val
    end
  end
end